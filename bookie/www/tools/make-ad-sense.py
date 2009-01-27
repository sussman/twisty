#!/usr/bin/python

import sys
import os
import os.path
import re

adsense_data = """
<div id="adsense">
<script type="text/javascript"><!--
google_ad_client = "pub-0505104349866057";
google_ad_width = 120;
google_ad_height = 600;
google_ad_format = "120x600_as";
google_ad_type = "text_image";
google_ad_channel ="";
google_color_border = "CC99CC";
google_color_bg = "E7C6E8";
google_color_link = "000000";
google_color_url = "00008B";
google_color_text = "663366";
//--></script>
<script type="text/javascript"
src="http://pagead2.googlesyndication.com/pagead/show_ads.js">
</script>
</div>
"""

adsense_css = """
/* Added for AdSense Support */
body
{
    margin-left: 130px;
    margin-right: 130px;
}
#adsense
{
    position: absolute;
    left: 0px; 
    top: 0.5in;
    width: 120px;
    z-index: 2;
}
"""

def die(msg):
    sys.stderr.write(msg + "\n")
    sys.exit(1)

_body_open_re = re.compile('^(.*<body[^>]*>)(.*)$')

def add_adsense_html(file):
    lines = open(file, 'r').readlines()
    for i in range(len(lines)):
        match = _body_open_re.match(lines[i])
        if match and match.groups():
            lines[i] = '%s%s%s' \
                       % (match.group(1), adsense_data, match.group(2))
            open(file, 'w').writelines(lines)
            return
    raise Exception, "Never found <body> tag in file '%s'" % (file)

def add_adsense_css(file):
    open(file, 'a').write(adsense_css)

def main():
    if len(sys.argv) != 2:
        die("Usage: %s BOOK-DIR" % (os.path.basename(sys.argv[0])))
    book_dir = sys.argv[1]
    stylesheet = os.path.join(book_dir, 'styles.css')
    if not os.path.exists(stylesheet):
        die("Book stylesheet is missing.")
    for child in os.listdir(sys.argv[1]):
        if child[-5:] == '.html':
            add_adsense_html(os.path.join(book_dir, child))
    add_adsense_css(stylesheet)

if __name__ == "__main__":
    main()
        
        
        
