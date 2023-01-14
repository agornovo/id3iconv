package net.zhoufeng;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import org.springframework.stereotype.Service;

import de.vdheide.mp3.ID3;
import de.vdheide.mp3.ID3v2;
import de.vdheide.mp3.ID3v2DecompressionException;
import de.vdheide.mp3.ID3v2Frame;
import de.vdheide.mp3.ID3v2IllegalVersionException;
import de.vdheide.mp3.ID3v2WrongCRCException;
import de.vdheide.mp3.NoID3TagException;
import de.vdheide.mp3.NoID3v2TagException;
import lombok.Getter;
import lombok.Setter;

@Service
@Getter
@Setter
public class Converter {
    protected static final String[] GENRE_STRING_ARRAY = { "Blues", "Classic Rock",
            "Country", "Dance", "Disco", "Funk", "Grunge", "Hip-Hop", "Jazz", "Metal",
            "New Age", "Oldies", "Other", "Pop", "R&B", "Rap", "Reggae", "Rock", "Techno",
            "Industrial", "Alternative", "Ska", "Death Metal", "Pranks", "Soundtrack",
            "Euro-Techno", "Ambient", "Trip-Hop", "Vocal", "Jazz Funk", "Fusion",
            "Trance", "Classical", "Instrumental", "Acid", "House", "Game", "Sound Clip",
            "Gospel", "Noise", "Alternative Rock", "Bass", "Soul", "Punk", "Space",
            "Meditative", "Instrumental Pop", "Instrumental Rock", "Ethnic", "Gothic",
            "Darkwave", "Techno-Industrial", "Electronic", "Pop-Folk", "Eurodance",
            "Dream", "Southern Rock", "Comedy", "Cult", "Gangsta", "Top 40",
            "Christian Rap", "Pop/Funk", "Jungle", "Native American", "Cabaret",
            "New Wave", "Psychadelic", "Rave", "Showtunes", "Trailer", "Lo-Fi", "Tribal",
            "Acid Punk", "Acid Jazz", "Polka", "Retro", "Musical", "Rock & Roll",
            "Hard Rock", "Folk", "Folk/Rock", "National Folk", "Swing", "Fast Fusion",
            "Bebob", "Latin", "Revival", "Celtic", "Bluegrass", "Avantgarde",
            "Gothic Rock", "Progressive Rock", "Psychedelic Rock", "Symphonic Rock",
            "Slow Rock", "Big Band", "Chorus", "Easy Listening", "Acoustic", "Humour",
            "Speech", "Chanson", "Opera", "Chamber Music", "Sonata", "Symphony",
            "Booty Bass", "Primus", "Porn Groove", "Satire", "Slow Jam", "Club", "Tango",
            "Samba", "Folklore", "Ballad", "Power Ballad", "Rhythmic Soul", "Freestyle",
            "Duet", "Punk Rock", "Drum Solo", "A Capella", "Euro-House", "Dance Hall",
            "Goa", "Drum & Bass", "Club-House", "Hardcore", "Terror", "Indie", "BritPop",
            "Negerpunk", "Polsk Punk", "Beat", "Christian Gangsta Rap", "Heavy Metal",
            "Black Metal", "Crossover", "Contemporary Christian", "Christian Rock",
            "Merengue", "Salsa", "Thrash Metal", "Anime", "JPop", "Synthpop" };
    private static final String[] NON_UNICODE_FIELDS = { "TDAT", "TIME", "TPOS", "TRCK",
            "TYER" };
    private static HashSet<String> nonUnicodeFields = new HashSet<>();
    static {
        for (int i = 0; i < NON_UNICODE_FIELDS.length; i++)
            nonUnicodeFields.add(NON_UNICODE_FIELDS[i]);
    }
    private boolean dry;
    private String encoding = System.getProperty("file.encoding");
    private boolean forceV1asSource;
    private ID3 id3v1;
    private ID3v2 id3v2;
    private boolean isDebug;
    private boolean quiet;
    private boolean removeV1;

    protected void addFrame(String frameId, String aString)
            throws UnsupportedEncodingException, ID3v2DecompressionException {
        debug(frameId + ": " + aString);
        byte[] content = getUnicodeLittleByteArrayOf(aString);
        if (content.length == 0)
            return;
        ID3v2Frame frame = getFrame(frameId, getContentByteArray(content), false, false,
                false, ID3v2Frame.NO_COMPRESSION, (byte) 0, (byte) 0);
        id3v2.addFrame(frame);
    }

    protected void addFrames() throws UnsupportedEncodingException,
            ID3v2DecompressionException, NoID3TagException {
        addFrame("TALB", id3v1.getAlbum());
        addFrame("TOPE", id3v1.getArtist());
        addFrame("TPE1", id3v1.getArtist());
        addFrame("COMM", id3v1.getComment());
        addFrame("TIT2", id3v1.getTitle());
        addFrame("TORY", id3v1.getYear());
        addGenre();
        addFrame("TRCK", id3v1.getTrack() + "");
    }

    protected void addGenre() throws NoID3TagException, UnsupportedEncodingException,
            ID3v2DecompressionException {
        int i = id3v1.getGenre();
        if (i >= 0 && i < GENRE_STRING_ARRAY.length)
            addFrame("TCON", GENRE_STRING_ARRAY[i]);
        else
            addFrame("TCON", "unknown");
    }

    /**
     * Convert the ID3 tag from any encoding to Unicode. If the original file
     * contains ID3 v1 tags, it is converted to ID3v2 in order to support Unicode.
     */
    protected void convert(File file) throws ID3v2IllegalVersionException,
            ID3v2WrongCRCException, ID3v2DecompressionException, IOException,
            NoID3TagException, NoID3v2TagException {
        id3v1 = getId3v1Tag(file);
        id3v1.encoding = encoding;
        id3v2 = getId3v2Tag(file);

        boolean hasv1 = id3v1.checkForTag();
        boolean hasv2 = hasV2Tags();

        if (shouldForceV1toV2TagConversion(hasv1, hasv2)) {
            convertId3v1toId3v2Tag(hasv2);
        } else if (hasv2) {
            reencodeId3v2TagToUnicode();
        } else {
            error("File " + file.getAbsolutePath() + " has no id3 tag, skipping!");
        }
    }

    protected void convertId3v1toId3v2Tag(boolean hasv2)
            throws UnsupportedEncodingException, ID3v2DecompressionException,
            NoID3TagException, IOException {
        // convert ID3v1 to ID3v2
        info("Converting id3v1 tag to id3v2 Unicode format.");
        if (hasV2andForcesV1asSource(hasv2)) {
            info("Warning: v1 tag use forced, original v2 tag overwritten.");
        }
        id3v2.clear(); // clear current v2 content, if it exists
        addFrames();
        removeV1tag();
        updateV2tag();
    }

    protected void debug(String string) {
        if (isDebug) {
            System.err.println(string);
        }
    }

    protected void error(String string) {
        System.out.println(string);
    }

    protected byte[] getContentByteArray(byte[] newbuf) {
        byte[] newbuf2 = new byte[newbuf.length + 3];
        System.arraycopy(newbuf, 0, newbuf2, 1, newbuf.length);
        newbuf2[newbuf2.length - 2] = newbuf2[newbuf2.length - 1] = 0;
        newbuf2[0] = 1; // encoding: utf-16
        return newbuf2;
    }

    protected ID3v2Frame getFrame(String frameId, byte[] content,
            boolean tag_alter_preservation, boolean file_alter_preservation,
            boolean read_only, byte compression_type, byte encryption_id, byte group)
            throws ID3v2DecompressionException {
        return new ID3v2Frame(frameId, content, tag_alter_preservation,
                file_alter_preservation, read_only, compression_type, encryption_id,
                group);
    }

    protected ID3 getId3v1Tag(File file) {
        return new ID3(file);
    }

    protected ID3v2 getId3v2Tag(File file)
            throws IOException, ID3v2IllegalVersionException, ID3v2WrongCRCException,
            ID3v2DecompressionException {
        return new ID3v2(file);
    }

    protected byte[] getNewContentByteArray(byte[] unicodeVersionByteArray) {
        byte[] newContentByteArray = new byte[unicodeVersionByteArray.length + 5];
        System.arraycopy(unicodeVersionByteArray, 0, newContentByteArray, 1,
                unicodeVersionByteArray.length);
        newContentByteArray[newContentByteArray.length
                - 2] = newContentByteArray[newContentByteArray.length - 1] = 0;
        newContentByteArray[0] = 1; // UNICODE encoding
        return newContentByteArray;
    }

    protected String getOriginalContent(byte[] originalContentByteArray)
            throws UnsupportedEncodingException {
        return new String(originalContentByteArray, 1,
                originalContentByteArray.length - 1, encoding);
    }

    protected byte[] getUnicodeLittleByteArrayOf(String aString)
            throws UnsupportedEncodingException {
        // UTF-16LE with leading BOM character
        // This seems to be the most compatible one
        return aString.getBytes("UnicodeLittle");
    }

    protected boolean hasV1andDoesNotHaveV2(boolean hasv1, boolean hasv2) {
        return hasv1 && !hasv2;
    }

    protected boolean hasV1andForcesV1asSource(boolean hasv1) {
        return hasv1 && forceV1asSource;
    }

    protected boolean hasV2andForcesV1asSource(boolean hasv2) {
        return hasv2 && forceV1asSource;
    }

    protected boolean hasV2tagAndV1tagIsNotForcedAsSource(boolean hasv2) {
        return hasv2 && !forceV1asSource;
    }

    protected boolean hasV2Tags() {
        boolean hasv2 = id3v2.hasTag();
        if (hasV2tagAndV1tagIsNotForcedAsSource(hasv2))
            try {
                id3v2.getFrames();
            } catch (Exception e) {
                debug("Cannot get v2 frames, assuming no v2 tag.");
                hasv2 = false;
            }
        return hasv2;
    }

    protected void info(String string) {
        if (!quiet) {
            System.out.println(string);
        }
    }

    protected boolean isNumericalStringOrUrl(ID3v2Frame frame) {
        /*
         * check whether the frame is "numerical string" or URL as defined by IDv2.3.
         * They should be encoded in ISO8859-1, not Unicode
         */
        return id3v2.getVersion() == 3 && nonUnicodeFields.contains(frame.getID());
    }

    protected boolean isText(ID3v2Frame frame) {
        return frame.getID().startsWith("T");
    }

    protected boolean reencode(ID3v2Frame frame) throws UnsupportedEncodingException {
        byte[] originalContentByteArray = frame.getContent();
        if (shouldBeReenconded(originalContentByteArray)) {
            String originalContent = getOriginalContent(originalContentByteArray);
            debug(frame.getID() + ": " + originalContent);
            byte[] unicodeVersionByteArray = getUnicodeLittleByteArrayOf(originalContent);
            byte[] newContentByteArray = getNewContentByteArray(unicodeVersionByteArray);
            frame.setContent(newContentByteArray);
            return (true);
        }
        return false;
    }

    protected void reencodeAndUpdate(Vector framesToReencode)
            throws UnsupportedEncodingException, IOException {
        boolean isReencoded = false;
        for (Iterator iter = framesToReencode.iterator(); iter.hasNext();) {
            ID3v2Frame frame = (ID3v2Frame) iter.next();
            isReencoded = reencodeFrame(frame);
        }
        if (shouldId3V2BeUpdated(isReencoded)) {
            id3v2.touch();
            id3v2.update();
        }
        removeV1tag();
    }

    protected boolean reencodeFrame(ID3v2Frame frame)
            throws UnsupportedEncodingException {
        if (isText(frame)) {
            if (isNumericalStringOrUrl(frame)) {
                debug("No action for frame: " + frame.getID()
                        + " because it's a v2.3 non-unicode field");
            } else {
                return (reencode(frame));
            }
        } else {
            debug("No action for frame: " + frame.getID());
        }
        return false;
    }

    protected void reencodeId3v2TagToUnicode()
            throws NoID3v2TagException, UnsupportedEncodingException, IOException {
        // convert all text frames
        info("Reencoding id3v2 tag into Unicode");

        Vector frames = id3v2.getFrames();
        if (thereAreFrames(frames)) {
            reencodeAndUpdate(frames);
        }
    }

    protected void removeV1tag() throws IOException {
        if (!dry && removeV1)
            id3v1.removeTag();
    }

    protected boolean shouldBeReenconded(byte[] originalContentByteArray) {
        return originalContentByteArray.length > 1 && originalContentByteArray[0] == 0;
    }

    protected boolean shouldForceV1toV2TagConversion(boolean hasv1, boolean hasv2) {
        return hasV1andDoesNotHaveV2(hasv1, hasv2) || hasV1andForcesV1asSource(hasv1);
    }

    protected boolean shouldId3V2BeUpdated(boolean updated) {
        return !dry && updated;
    }

    protected boolean thereAreFrames(Vector frames) {
        return frames != null && frames.size() > 0;
    }

    protected void updateV2tag() throws IOException {
        if (!dry)
            id3v2.update();
    }
}
