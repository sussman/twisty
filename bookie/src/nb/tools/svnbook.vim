" $Revision: 1573 $
" $Date: 2005-07-29 09:03:08 -0500 (Fri, 29 Jul 2005) $

" Vim syntax file for the Norwegian XML files in the Subversion Book.

" Options

set fenc=utf8
set tw=72
set fo+=w fo-=2
set ts=2 sw=2 sts=2 et
set si
set cinw=<para>,<variablelist>,<varlistentry>,<orderedlist>,<itemizedlist>,<listitem>,<simplesect>,<chapter,<note>,<figure,<sect1,<sect2,<sidebar,<figure,<table,<tgroup,<thead,<row>,<tbody>,<entry>,<title>
set fdm=marker
set nowrap

" Mappings for translation work

" F5: Mark a paragraph in linewise visual mode and press F5 to comment 
" out the English text and copy all the elements and go into insert mode 
" after the first element. Expects the dings_it Perl script to be placed 
" as ~/bin/dings_it .
noremap <f5> :!~/bin/dings_it<cr>zo/@ENGLISH }}}<cr>j0f>a

" F6: Mark a paragraph of the translated text in linevise visual mode 
" and press F6 to let Vim reformat the paragraph, then search to the 
" next ENGLISH marker and go into linewise visual mode again.
noremap <f6> gq/@ENGLISH }}}<cr>jV

" F7: Same as F6, but instead of searching for the next @ENGLISH marker, 
" the next change is found.
noremap <f7> gq]ckV

runtime! syntax/xml.vim

" vim: set fdm=manual tw=72 ts=2 sw=2 sts=2 et fenc=utf8 :
