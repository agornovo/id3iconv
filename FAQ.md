Copied from http://zhoufeng.net/eng/id3iconv (which was last updated on 2004/11/17, according to the time stamp on the page, so some of these answers may be outdated).

#What does it actually do to my files?

ID3iconv takes all text fields of your tag, assume they are in a certain encoding (specified by -e or using system default), and convert them to their Unicode equivalence as supported by ID3 v2.3. If the file has only v1 tag before, a v2 tag will be prepended to the beginning of the file. If the file has non-unicode v2 tag before, it will be converted to unicode format. You also have the option to remove any existing v1 tag after conversion is done. The output encoding is Unicode little endian, which seems to be the most compatible.

#Does it convert all fields of the tag?

It converts all text fields (fields with name Txxx in ID3v2), except those marked as numerical text, such as TYER, as defined in ID3 v2.3 text.

#Does it also convert file names to Unicode?

No. However, you can try convmv (a Perl tool) for that.

#What encoding does ID3iconv assume the original tags are in?

The encoding can be specified via the -e option, using Java encoding names, as listed here. If no encoding is specified, the default OS encoding will be used. This is normally determined by the locale setting of your operating system. E.g., on *NIX, look at LC_CTYPE output of 'locale'.

#What players can view tags in converted mp3 files?

Unfortunately a lot of players don't handle Unicode tags well, mostly because these files are rare, although there should be more. Here's my results.

##Players that handle unicode tags correctly
- Rhythmbox 0.6.5. Gnome's default music player. (Note: This bug tracks support for native charsets)
- iTunes for windows.
- Windows explorer's file property dialog for MP3 files

##Players that I haven't got working with Unicode tags
- Winamp 5. It seems to be treating all tags as the machine's native encoding, even if they are clearly marked as in Unicode
- Foobar 0.7.7b w and w/o id3v2 component. The Id3v2 component also generates wrongly-formatted Unicode tags (missing leading BOM characters), which only itself can recognize. :(
- XMMS (One workaround is to use id3v1 tags by disabling v2 tag support, Open options -> preferences -> Audio I/O plugins -> MPEG Layer 1/2/3 Player and click on "configure" at the "title" tab you will have an option "Disable ID3V2 tag" - chek it and xmms will use old ID3V1 tags. Thanks go to Alexandr Dmitrijev for pointing this out).