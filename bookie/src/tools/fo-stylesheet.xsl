<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version='1.0'>

  <xsl:import href="xsl/fo/docbook.xsl"/>

  <xsl:param name="fop.extensions" select="1" />
  <xsl:param name="variablelist.as.blocks" select="1" />
  <xsl:param name="admon.graphics" select="1" />
  <xsl:param name="admon.graphics.path">images/</xsl:param>
  <xsl:param name="admon.graphics.extension">.png</xsl:param>
</xsl:stylesheet>
