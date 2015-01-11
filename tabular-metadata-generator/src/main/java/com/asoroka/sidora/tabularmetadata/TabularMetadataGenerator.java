
package com.asoroka.sidora.tabularmetadata;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.collect.Iterators.peekingIterator;
import static com.google.common.collect.Lists.newArrayList;
import static com.googlecode.totallylazy.Sequences.sequence;
import static org.apache.commons.csv.CSVFormat.DEFAULT;
import static org.apache.commons.csv.CSVParser.parse;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.asoroka.sidora.tabularmetadata.datatype.DataType;
import com.asoroka.sidora.tabularmetadata.formats.TabularFormat;
import com.asoroka.sidora.tabularmetadata.heuristics.Heuristic.Extract;
import com.asoroka.sidora.tabularmetadata.heuristics.enumerations.EnumeratedValuesHeuristic;
import com.asoroka.sidora.tabularmetadata.heuristics.enumerations.LimitedEnumeratedValuesHeuristic;
import com.asoroka.sidora.tabularmetadata.heuristics.headers.DefaultHeaderHeuristic;
import com.asoroka.sidora.tabularmetadata.heuristics.headers.HeaderHeuristic;
import com.asoroka.sidora.tabularmetadata.heuristics.ranges.RangeDeterminingHeuristic;
import com.asoroka.sidora.tabularmetadata.heuristics.ranges.RunningMinMaxHeuristic;
import com.asoroka.sidora.tabularmetadata.heuristics.types.StrictHeuristic;
import com.asoroka.sidora.tabularmetadata.heuristics.types.TypeDeterminingHeuristic;
import com.google.common.collect.PeekingIterator;
import com.google.common.collect.Range;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.numbers.Ratio;

/**
 * Master entry point for this API. This is the class from which all parsing should initiate, via
 * {@link #getMetadata(URL)}.
 * 
 * @author A. Soroka
 */
public class TabularMetadataGenerator {

    private static final Charset CHARACTER_ENCODING = UTF_8;

    private CSVFormat format = DEFAULT;

    /**
     * Default value of {@code 0} indicates no limit. See {@link TabularScanner.scan(final int limit)}.
     */
    private Integer scanLimit = 0;

    private RangeDeterminingHeuristic<?> rangeStrategy = new RunningMinMaxHeuristic();

    private TypeDeterminingHeuristic<?> typeStrategy = new StrictHeuristic();

    private EnumeratedValuesHeuristic<?> enumStrategy = new LimitedEnumeratedValuesHeuristic();

    private HeaderHeuristic<?> headerStrategy = new DefaultHeaderHeuristic();

    /**
     * The main entry point to application workflow.
     * 
     * @param dataUrl Where to find some tabular data.
     * @return The results of metadata extraction.
     * @throws IOException
     */
    public TabularMetadata getMetadata(final URL dataUrl) throws IOException {

        try (final CSVParser csvParser = parse(dataUrl, CHARACTER_ENCODING, format)) {
            final PeekingIterator<CSVRecord> parser = peekingIterator(csvParser.iterator());
            // TODO allow a HeaderHeuristic to use more information than the first line of data
            final CSVRecord firstLine = parser.peek();
            for (final String field : firstLine) {
                headerStrategy.addValue(field);
            }
            final boolean hasHeaders = headerStrategy.results();
            headerStrategy.reset();
            final List<String> headerNames;
            if (hasHeaders) {
                headerNames = newArrayList(firstLine);
                parser.next();
            }
            else {
                headerNames = emptyHeaders(firstLine.size());
            }
            // scan values up to the limit
            final TabularScanner scanner = new TabularScanner(parser, typeStrategy, rangeStrategy, enumStrategy);
            scanner.scan(scanLimit);

            final List<TypeDeterminingHeuristic<?>> typeStrategies = scanner.getTypeStrategies();
            final List<RangeDeterminingHeuristic<?>> rangeStrategies = scanner.getRangeStrategies();
            final List<EnumeratedValuesHeuristic<?>> enumStrategies = scanner.getEnumStrategies();

            // extract the results for each field
            final List<DataType> columnTypes = sequence(typeStrategies).map(new Extract<DataType>()).toList();
            final List<Map<DataType, Range<?>>> minMaxes =
                    sequence(rangeStrategies).map(new Extract<Map<DataType, Range<?>>>()).toList();
            final List<Map<DataType, Set<String>>> enumValues =
                    sequence(enumStrategies).map(new Extract<Map<DataType, Set<String>>>()).toList();
            final List<Ratio> valuesSeen = sequence(typeStrategies).map(EXTRACT_RATIOS).toList();
            return new TabularMetadata(headerNames, valuesSeen, columnTypes, minMaxes, enumValues);
        } catch (final NoSuchElementException e) {
            throw new EmptyDataFileException(dataUrl + " has no data in it!");
        }
    }

    private static final List<String> emptyHeaders(final int numFields) {
        final List<String> headers = new ArrayList<>(numFields);
        for (int i = 1; i <= numFields; i++) {
            headers.add("Variable " + i);
        }
        return headers;
    }

    private static final Callable1<TypeDeterminingHeuristic<?>, Ratio> EXTRACT_RATIOS =
            new Callable1<TypeDeterminingHeuristic<?>, Ratio>() {

                @Override
                public Ratio call(final TypeDeterminingHeuristic<?> h) {
                    return new Ratio(new BigInteger(Integer.toString(h.valuesSeen() - h.parseableValuesSeen())),
                            new BigInteger(Integer.toString(h
                                    .valuesSeen())));
                }
            };

    /**
     * @param scanLimit A limit to the number of rows to scan, including any header row that may be present. {@code 0}
     *        indicates no limit.
     */
    public void setScanLimit(final Integer scanLimit) {
        this.scanLimit = scanLimit;
    }

    /**
     * @param strategy The header recognition strategy to use.
     */
    @Inject
    public <H extends HeaderHeuristic<?>> void setHeaderStrategy(final H strategy) {
        this.headerStrategy = strategy;
    }

    /**
     * @param format The tabular data format to expect.
     */
    @Inject
    public void setFormat(final TabularFormat format) {
        this.format = format.get();
    }

    /**
     * @param strategy The type recognition strategy to use.
     */
    @Inject
    public void setTypeStrategy(final TypeDeterminingHeuristic<?> strategy) {
        this.typeStrategy = strategy;
    }

    /**
     * @param strategy The range recognition strategy to use.
     */
    @Inject
    public void setRangeStrategy(final RangeDeterminingHeuristic<?> strategy) {
        this.rangeStrategy = strategy;
    }

    /**
     * @param strategy The enumerated-values recognition strategy to use.
     */
    @Inject
    public void setEnumStrategy(final EnumeratedValuesHeuristic<?> strategy) {
        this.enumStrategy = strategy;
    }
}
