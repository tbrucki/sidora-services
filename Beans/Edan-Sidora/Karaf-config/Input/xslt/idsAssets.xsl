<!--
  ~ Copyright 2015-2016 Smithsonian Institution.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License"); you may not
  ~ use this file except in compliance with the License.You may obtain a copy of
  ~ the License at: http://www.apache.org/licenses/
  ~
  ~ This software and accompanying documentation is supplied without
  ~ warranty of any kind. The copyright holder and the Smithsonian Institution:
  ~ (1) expressly disclaim any warranties, express or implied, including but not
  ~ limited to any implied warranties of merchantability, fitness for a
  ~ particular purpose, title or non-infringement; (2) do not assume any legal
  ~ liability or responsibility for the accuracy, completeness, or usefulness of
  ~ the software; (3) do not represent that use of the software would not
  ~ infringe privately owned rights; (4) do not warrant that the software
  ~ is error-free or will be maintained, supported, updated or enhanced;
  ~ (5) will not be liable for any indirect, incidental, consequential special
  ~ or punitive damages of any kind or nature, including but not limited to lost
  ~ profits or loss of data, on any basis arising from contract, tort or
  ~ otherwise, even if any of the parties has been warned of the possibility of
  ~ such loss or damage.
  ~
  ~ This distribution includes several third-party libraries, each with their own
  ~ license terms. For a complete copy of all copyright and license terms, including
  ~ those of third-party libraries, please see the product release notes.
  -->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" version="1.0" encoding="ISO-8859-1" indent="yes"/>
    <xsl:strip-space elements="*"/>

    <xsl:param name="imageid"/>
    <xsl:param name="idsAssetImagePrefix"/>
    <xsl:param name="isPublic"/>
    <xsl:param name="isInternal"/>

    <!-- identity transform -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- match the root element of unknown name -->
    <xsl:template match="/*">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
            <!--check to make sure the asset needs added-->
            <xsl:if test="not(count(//Asset[text()=concat($idsAssetImagePrefix, $imageid)]) > 0)">
            <!-- add a new element at the end -->
            <Asset Name="{$idsAssetImagePrefix}{$imageid}.JPG" IsPublic="{$isPublic}" IsInternal="{$isInternal}" MaxSize="3000" InternalMaxSize="4000"><xsl:value-of
                    select="$idsAssetImagePrefix"/><xsl:value-of
                    select="$imageid"/></Asset>
            </xsl:if>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>