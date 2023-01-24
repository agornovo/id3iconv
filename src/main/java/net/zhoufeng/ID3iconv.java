/*
 * ID3iconv is a little Java command line tool to convert ID3 tags in mp3 files from whatever machine encoding you have (GB2312/GBK for Chinese, etc) to Unicode
 */

// Copyright (C) 2004 Feng Zhou

/*
 * This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License
   as published by the Free Software Foundation; either version 2
   of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package net.zhoufeng;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.Setter;

/**
 * @author zf
 *
 */
@SpringBootApplication
public class ID3iconv implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(ID3iconv.class, args);
    }

    @Autowired
    @Setter
    private Converter converter;

    protected File getFile(String pathname) {
        return new File(pathname);
    }

    protected Collection<File> listFiles(File directory,
            String[] fileExtensionsStringArray, boolean recursive) {
        return FileUtils.listFiles(directory, fileExtensionsStringArray, recursive);
    }

    protected void quit() {
        System.exit(-1);
    }

    @Override
    public void run(String... args) {
        int opt = 0;
        if (args.length == 0) {
            usage();
            return;
        }

        for (; opt < args.length; opt++) {
            String s = args[opt];
            if (s.equals("-e")) {
                converter.setEncoding(args[++opt]);
            } else if (s.equals("-p")) {
                converter.setDry(true);
            } else if (s.equals("-q")) {
                converter.setQuiet(true);
            } else if (s.equals("-v1")) {
                converter.setForceV1asSource(true);
            } else if (s.equals("-removev1")) {
                converter.setRemoveV1(true);
            } else if (s.equals("-d")) {
                converter.setDebug(true);
            } else if (s.startsWith("-")) {
                converter.error("Unknown option: " + s);
                quit();
            } else {
                break;
            }
        }

        converter.info("Using source encoding: " + converter.getEncoding());
        for (int i = opt; i < args.length; i++)
            try {
                converter.info("Converting " + args[i]);
                File file = getFile(args[i]);
                if (file.isDirectory()) {
                    Collection<File> files = listFiles(file, new String[] { "mp3" },
                            true);
                    for (File f : files) {
                        converter.convert(f);
                    }
                } else {
                    converter.convert(file);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    protected void usage() {
        System.out
                .println("ID3iconv - convert ID3 (ID3v1 or v2) tags from native encoding "
                        + "to unicode and store them using ID3v2 format.\n"
                        + "\n\tid3iconv [options] [mp3 files]\n\n"
                        + "Supported options:\n"
                        + "-e <encoding>   Specify original tag encoding.  If not specified, system default encoding will be used.\n"
                        + "-p              Dry-run. Do not actually modify files\n"
                        + "-v1             Force using v1 tag as source, even if v2 tag exists.  Default is using v2 tag.\n"
                        + "-removev1       Remove v1 tag after processing the file\n"
                        + "-q              Quiet mode\n"
                        + "-d              Output debug info to stderr\n"
                        + "\nCAUTION: Files are update in-place.  So backup if you're unsure of what you are doing.");
    }

}
