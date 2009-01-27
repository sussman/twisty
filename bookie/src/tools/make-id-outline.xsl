<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!--
  This is a stylesheet to provide an overview of the id attributes in
  use throughout the book.  It is intended to be run manually:
    $ xsltproc ../../tools/make-id-outline.xsl book.xml
  -->

  <xsl:output method="xml" indent="yes"/>

  <xsl:template match="book|sidebar|sect3|refentry|colophon|*[@id]|
    appendix|chapter|preface|part|sect1|sect2|figure|table|example|indexterm">
    <xsl:copy>
      <xsl:attribute name="id">
        <xsl:value-of select="@id"/>
      </xsl:attribute>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="text()"/>

</xsl:stylesheet>
