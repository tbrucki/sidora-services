<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright 2015-2016 Smithsonian Institution.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License"); you may not
  ~ use this file except in compliance with the License.You may obtain a copy of
  ~ the License at: "http://www.apache.org/licenses/
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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-result-prefixes="xs"
                version="1.0">
    <xsl:output method="text" encoding="UTF-8" media-type="text/plain"/>


    <xsl:param name="imageid"/>
    <xsl:param name="CamelFedoraPid"/>

    <xsl:template match="/">

        <xsl:apply-templates select="/CameraTrapDeployment">
            <xsl:with-param name="imageid"><xsl:value-of select="$imageid"/></xsl:with-param>
            <xsl:with-param name="CamelFedoraPid"><xsl:value-of select="$CamelFedoraPid"/></xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="CameraTrapDeployment">
        <xsl:param name="imageid"/>
        <xsl:param name="CamelFedoraPid"/>
        <xsl:text>{</xsl:text>
        <xsl:text>content: {</xsl:text>
        <xsl:text>"source": "dev",</xsl:text>
        <xsl:text>"project_id": "</xsl:text><xsl:value-of select="ProjectId"/><xsl:text>",</xsl:text>
        <xsl:text>"project_name": "</xsl:text><xsl:value-of select="ProjectName"/><xsl:text>",</xsl:text>
        <xsl:text>"sub_project_id": "</xsl:text><xsl:value-of select="SubProjectId"/><xsl:text>",</xsl:text>
        <xsl:text>"sub_project_name":"</xsl:text><xsl:value-of select="SubProjectName"/><xsl:text>",</xsl:text>
        <xsl:text>"deployment_id": "</xsl:text><xsl:value-of select="CameraDeploymentID"/><xsl:text>",</xsl:text>
        <xsl:text>"deployment_name": "</xsl:text><xsl:value-of select="CameraSiteName"/><xsl:text>",</xsl:text>
        <xsl:text>"image_sequence_id": "</xsl:text><xsl:value-of select="//ImageSequence[Image[ImageId=$imageid]]/ImageSequenceId"/><xsl:text>",</xsl:text>
        <xsl:text>"image": {</xsl:text>
        <xsl:text>"id": "</xsl:text><xsl:value-of select="$imageid"/><xsl:text>",</xsl:text>
        <xsl:text>"online_media": [</xsl:text>
        <xsl:text>{</xsl:text>
        <xsl:text>"content": "http://ids.si.edu/ids/deliveryService?id%5Cu003demammal_image_</xsl:text><xsl:value-of select="$imageid"/><xsl:text>",</xsl:text>
        <xsl:text>"idsId": "</xsl:text>emammal_image_<xsl:value-of select="$imageid"/><xsl:text>",</xsl:text>
        <xsl:text>"sidoraPid": "</xsl:text><xsl:value-of select="$CamelFedoraPid"/><xsl:text>",</xsl:text>
        <xsl:text>"type": "</xsl:text>Images<xsl:text>",</xsl:text>
        <xsl:text>"caption": "</xsl:text>Camera Trap Image <xsl:value-of select="//ImageSequence[Image[ImageId = $imageid]]/ResearcherIdentifications/Identification/SpeciesCommonName"/><xsl:text>",</xsl:text>
        <xsl:text>"thumbnail": "</xsl:text>http://ids.si.edu/ids/deliveryService?id%5Cu003demammal_image_<xsl:value-of select="$imageid"/><xsl:text>",</xsl:text>
        <xsl:text>}</xsl:text>
        <xsl:text>],</xsl:text>
        <xsl:text>"date_time": "</xsl:text><xsl:value-of select="//Image[ImageId=$imageid]/ImageDateTime"/><xsl:text>",</xsl:text>
        <xsl:text>"photo_type": "</xsl:text><xsl:value-of select="//Image[ImageId=$imageid]/PhotoType"/><xsl:text>",</xsl:text>
        <xsl:text>"photo_type_identified_by": "</xsl:text><xsl:value-of select="//Image[ImageId=$imageid]/PhotoTypeIdentifications/PhotoTypeIdentifiedBy"/><xsl:text>",</xsl:text>
        <xsl:text>"interest_ranking": "</xsl:text>None<xsl:text>",</xsl:text>
        <xsl:text>},</xsl:text>
        <xsl:text>"image_identifications": [</xsl:text>
        <xsl:text>{</xsl:text>
        <xsl:text>"iucn_id": "</xsl:text><xsl:value-of
                select="//ImageSequence[Image[ImageId = $imageid]]/ResearcherIdentifications/Identification/IUCNId"/><xsl:text>",</xsl:text>
        <xsl:text>"species_scientific_name": "</xsl:text><xsl:value-of select="//ImageSequence[Image[ImageId = $imageid]]/ResearcherIdentifications/Identification/SpeciesScientificName"/><xsl:text>",</xsl:text>
        <xsl:text>"individual_animal_notes": "</xsl:text><xsl:value-of
                select="//ImageSequence[Image[ImageId = $imageid]]/ResearcherIdentifications/Identification/IndividualAnimalNotes"/><xsl:text>",</xsl:text>
        <xsl:text>"species_common_name": "</xsl:text><xsl:value-of select="//ImageSequence[Image[ImageId = $imageid]]/ResearcherIdentifications/Identification/SpeciesCommonName"/><xsl:text>",</xsl:text>
        <xsl:text>"count": </xsl:text><xsl:value-of select="//ImageSequence[Image[ImageId = $imageid]]/ResearcherIdentifications/Identification/Count"/><xsl:text>,</xsl:text>
        <xsl:text>"age": "</xsl:text><xsl:value-of select="//ImageSequence[Image[ImageId = $imageid]]/ResearcherIdentifications/Identification/Age"/><xsl:text>",</xsl:text>
        <xsl:text>"sex": "</xsl:text><xsl:value-of select="//ImageSequence[Image[ImageId = $imageid]]/ResearcherIdentifications/Identification/Sex"/><xsl:text>",</xsl:text>
        <xsl:text>"individual_id": "</xsl:text><xsl:value-of select="//ImageSequence[Image[ImageId = $imageid]]/ResearcherIdentifications/Identification/IndividualId"/><xsl:text>",</xsl:text>
        <xsl:text>"animal_recognizable": "</xsl:text><xsl:value-of select="//ImageSequence[Image[ImageId = $imageid]]/ResearcherIdentifications/Identification/AnimalRecognizable"/><xsl:text>",</xsl:text>
        <xsl:text>}</xsl:text>
        <xsl:text>]</xsl:text>
        <xsl:text>},</xsl:text>

        <xsl:text>"publicSearch": false,</xsl:text>
        <xsl:text>"title": "</xsl:text>Camera Trap Image <xsl:value-of select="//ImageSequence[Image[ImageId = $imageid]]/ResearcherIdentifications/Identification/SpeciesCommonName"/><xsl:text>",</xsl:text>
        <xsl:text>"type": "</xsl:text>emammal_image<xsl:text>",</xsl:text>
        <xsl:text>"url": "</xsl:text><xsl:value-of select="$imageid"/><xsl:text>",</xsl:text>
        <xsl:text>}</xsl:text>
    </xsl:template>

</xsl:stylesheet>