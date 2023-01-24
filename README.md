#ID3iconv
ID3iconv is a little Java command line tool to convert ID3 tags in mp3 files from whatever machine encoding you have (GB2312/GBK for Chinese, etc) to Unicode. It convert both ID3v1 tags and ID3v2 tags to Unicode-encoded ID3v2 (v2.3). This is sometimes useful because,

- ID3v1 or v2 don't really supports multi-byte encodings such as GBK or Big5. Most existing files falsely pretend they are ISO-8859-1 encoded. This means the softwares handle them in all kinds of weird ways.
- Even if the user can force the encoding in some players, it is then impossible to display tags of several international languages at the same time if files are so encoded.

I wrote this to convert all my mp3 files with Chinese (both simplified and traditional Chinese) tags to Unicode and play them using Rhythmbox under Linux. This is after much struggle with XMMS and other players that either cannot handle the large charsets GBK/GB18030 or cannot handle Chinese at all.

CAUTION: The tool updates mp3 files in place. So backup if you don't want to lose your precious music...

Author: Feng Zhou

Information about ID3iconv version 0.2.1, along with an FAQ, is available at 
http://zhoufeng.net/eng/id3iconv

#Getting started
Make sure you have at least a Java 17 Java Runtime Environment (JRE) installed in your environment.  Get started by running the following command.
The command prompt you with supported command-line options.

java -jar id3iconv-0.0.1-SNAPSHOT-exec.jar

Replace the id3iconv-0.0.1-SNAPSHOT-exec.jar with the latest *-exec.jar file name available.

#Test mp3 file
Empty mp3 file, used for testing, copied from https://github.com/anars/blank-audio