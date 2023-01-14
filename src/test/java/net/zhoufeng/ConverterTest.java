package net.zhoufeng;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import de.vdheide.mp3.ID3;
import de.vdheide.mp3.ID3v2;
import de.vdheide.mp3.ID3v2DecompressionException;
import de.vdheide.mp3.ID3v2Frame;
import de.vdheide.mp3.ID3v2IllegalVersionException;
import de.vdheide.mp3.ID3v2WrongCRCException;
import de.vdheide.mp3.NoID3TagException;
import de.vdheide.mp3.NoID3v2TagException;

class ConverterTest {
    private static final String EMPTY_STRING = "";
    private static final Integer NOT_3 = 456;
    private static final int NOT_ZERO = 9;
    private static final String PRESET_ALBUM = "PRESET_ALBUM";
    private static final String PRESET_ARTIST = "PRESET_ARTIST";
    private static final boolean PRESET_BOOLEAN = false;
    private static final String PRESET_COMMENT = "PRESET_COMMENT";
    private static final String PRESET_FRAME_ID = "PRESET_FRAME_ID";
    private static final Integer PRESET_GENRE = 5;
    private static final String PRESET_STRING = "PRESET_STRING";
    private static final byte[] PRESET_BYTE_ARRAY = PRESET_STRING.getBytes();
    private static final String PRESET_TITLE = "PRESET_TITLE";
    private static final Integer PRESET_TRACK = 42;
    private static final String PRESET_YEAR = "PRESET_YEAR";
    private static final int RANDOM_BYTE = 1;
    private static final String RANDOM_FILE_EXTENSION = "RANDOM_FILE_EXTENSION";
    private static final String RANDOM_FILE_NAME = "RANDOM_FILE_NAME";
    private static final String RANDOM_ID = "RANDOM_ID";
    private static final String RANDOM_STRING = "RANDOM_STRING";
    private static final Integer THREE = 3;
    private static final Integer UNKNOWN_GENRE_LOWER_RANGE = -1;
    private static final int UNKNOWN_GENRE_UPPER_RANGE = 65000;
    private static final int ZERO = 0;

    @InjectMocks
    private Converter classUnderTest;
    private AutoCloseable closeable;
    @Mock
    private File mockFile;
    @Mock
    private Vector<?> mockFrames;
    @Mock
    private ID3 mockId3V1Tag;
    @Mock
    private ID3v2Frame mockId3V2Frame;
    @Mock
    private ID3v2 mockId3V2Tag;
    @Mock
    private InputStream mockInputStream;
    @Spy
    @InjectMocks
    private Converter spy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    private void setupConvertMethodMocks()
            throws IOException, ID3v2IllegalVersionException, ID3v2WrongCRCException,
            ID3v2DecompressionException {
        doReturn(mockId3V1Tag).when(spy).getId3v1Tag(mockFile);
        doReturn(mockId3V2Tag).when(spy).getId3v2Tag(mockFile);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testAddFrame_adds_frame()
            throws UnsupportedEncodingException, ID3v2DecompressionException {
        spy.setId3v2(mockId3V2Tag);
        doReturn(PRESET_BYTE_ARRAY).when(spy).getUnicodeLittleByteArrayOf(PRESET_STRING);
        spy.addFrame(PRESET_FRAME_ID, PRESET_STRING);
        verify(spy, times(1)).getUnicodeLittleByteArrayOf(PRESET_STRING);
        final byte[] presetContent = spy.getContentByteArray(PRESET_BYTE_ARRAY);
        verify(spy, times(1)).getFrame(PRESET_FRAME_ID, presetContent, false, false,
                false, ID3v2Frame.NO_COMPRESSION, (byte) 0, (byte) 0);
        verify(mockId3V2Tag, times(1)).addFrame(any(ID3v2Frame.class));
    }

    @Test
    void testAddFrame_returns_if_no_content()
            throws UnsupportedEncodingException, ID3v2DecompressionException {
        spy.setId3v2(mockId3V2Tag);
        byte[] ZeroLengthByteArray = EMPTY_STRING.getBytes();
        doReturn(ZeroLengthByteArray).when(spy).getUnicodeLittleByteArrayOf(EMPTY_STRING);
        spy.addFrame(RANDOM_ID, EMPTY_STRING);
        verify(mockId3V2Tag, never()).addFrame(any(ID3v2Frame.class));
    }

    @Test
    void testAddFrames() throws UnsupportedEncodingException, ID3v2DecompressionException,
            NoID3TagException {
        spy.setId3v1(mockId3V1Tag);
        when(mockId3V1Tag.getAlbum()).thenReturn(PRESET_ALBUM);
        when(mockId3V1Tag.getArtist()).thenReturn(PRESET_ARTIST);
        when(mockId3V1Tag.getComment()).thenReturn(PRESET_COMMENT);
        when(mockId3V1Tag.getTitle()).thenReturn(PRESET_TITLE);
        when(mockId3V1Tag.getYear()).thenReturn(PRESET_YEAR);
        when(mockId3V1Tag.getTrack()).thenReturn(PRESET_TRACK);
        doNothing().when(spy).addFrame(any(String.class), any(String.class));
        spy.addFrames();
        verify(spy, times(1)).addFrame("TALB", PRESET_ALBUM);
        verify(spy, times(1)).addFrame("TOPE", PRESET_ARTIST);
        verify(spy, times(1)).addFrame("TPE1", PRESET_ARTIST);
        verify(spy, times(1)).addFrame("COMM", PRESET_COMMENT);
        verify(spy, times(1)).addFrame("TIT2", PRESET_TITLE);
        verify(spy, times(1)).addFrame("TORY", PRESET_YEAR);
        verify(spy, times(1)).addGenre();
        verify(spy, times(1)).addFrame("TRCK", String.valueOf(PRESET_TRACK));
    }

    @Test
    void testAddGenre() throws NoID3TagException, UnsupportedEncodingException,
            ID3v2DecompressionException {
        spy.setId3v1(mockId3V1Tag);
        when(mockId3V1Tag.getGenre()).thenReturn(PRESET_GENRE);
        spy.addGenre();
        verify(spy, times(1)).addFrame("TCON",
                Converter.GENRE_STRING_ARRAY[PRESET_GENRE]);
    }

    @Test
    void testAddGenre_adds_unknown_lower_range() throws NoID3TagException,
            UnsupportedEncodingException, ID3v2DecompressionException {
        spy.setId3v1(mockId3V1Tag);
        when(mockId3V1Tag.getGenre()).thenReturn(UNKNOWN_GENRE_LOWER_RANGE);
        spy.addGenre();
        verify(spy, times(1)).addFrame("TCON", "unknown");
    }

    @Test
    void testAddGenre_adds_unknown_upper_range() throws NoID3TagException,
            UnsupportedEncodingException, ID3v2DecompressionException {
        spy.setId3v1(mockId3V1Tag);
        when(mockId3V1Tag.getGenre()).thenReturn(UNKNOWN_GENRE_UPPER_RANGE);
        spy.addGenre();
        verify(spy, times(1)).addFrame("TCON", "unknown");
    }

    @Test
    void testConvert_checks_if_file_has_any_id3_tags_at_all()
            throws ID3v2IllegalVersionException, ID3v2WrongCRCException,
            ID3v2DecompressionException, NoID3TagException, NoID3v2TagException,
            IOException {
        setupConvertMethodMocks();
        doReturn(false).when(spy).shouldForceV1toV2TagConversion(any(Boolean.class),
                any(Boolean.class));
        doReturn(false).when(spy).hasV2Tags();
        spy.convert(mockFile);
    }

    @Test
    void testConvert_converts_v1_to_v2() throws ID3v2IllegalVersionException,
            ID3v2WrongCRCException, ID3v2DecompressionException, NoID3TagException,
            NoID3v2TagException, IOException {
        setupConvertMethodMocks();
        when(mockId3V1Tag.checkForTag()).thenReturn(PRESET_BOOLEAN);
        doReturn(PRESET_BOOLEAN).when(spy).hasV2Tags();
        doReturn(true).when(spy).shouldForceV1toV2TagConversion(any(Boolean.class),
                any(Boolean.class));
        doNothing().when(spy).convertId3v1toId3v2Tag(any(Boolean.class));
        spy.convert(mockFile);
        verify(spy, times(1)).convertId3v1toId3v2Tag(any(Boolean.class));
    }

    @Test
    void testConvert_reencodes_id3v2_tag_to_unicode() throws ID3v2IllegalVersionException,
            ID3v2WrongCRCException, ID3v2DecompressionException, NoID3TagException,
            NoID3v2TagException, IOException {
        setupConvertMethodMocks();
        doReturn(true).when(spy).hasV2Tags();
        doReturn(false).when(spy).shouldForceV1toV2TagConversion(any(Boolean.class),
                any(Boolean.class));
        doNothing().when(spy).reencodeId3v2TagToUnicode();
        spy.convert(mockFile);
        verify(spy, times(1)).reencodeId3v2TagToUnicode();
    }

    @Test
    void testConvertId3v1toId3v2Tag() throws UnsupportedEncodingException,
            ID3v2DecompressionException, NoID3TagException, IOException {
        doReturn(false).when(spy).hasV2andForcesV1asSource(PRESET_BOOLEAN);
        spy.setId3v2(mockId3V2Tag);
        doNothing().when(spy).addFrames();
        doNothing().when(spy).removeV1tag();
        doNothing().when(spy).updateV2tag();
        spy.convertId3v1toId3v2Tag(PRESET_BOOLEAN);
        verify(mockId3V2Tag, times(1)).clear();
        verify(spy, times(1)).addFrames();
        verify(spy, times(1)).removeV1tag();
        verify(spy, times(1)).updateV2tag();
    }

    @Test
    void testConvertId3v1toId3v2Tag_does_not_issue_a_warning_when_original_v2_tag_is_untouched()
            throws UnsupportedEncodingException, ID3v2DecompressionException,
            NoID3TagException, IOException {
        doReturn(false).when(spy).hasV2andForcesV1asSource(PRESET_BOOLEAN);
        doNothing().when(spy).addFrames();
        doNothing().when(spy).removeV1tag();
        doNothing().when(spy).updateV2tag();
        spy.convertId3v1toId3v2Tag(PRESET_BOOLEAN);
        verify(spy, times(1)).info(any(String.class));
    }

    @Test
    void testConvertId3v1toId3v2Tag_issues_a_warning_when_original_v2_tag_is_overwritten_from_v1()
            throws UnsupportedEncodingException, ID3v2DecompressionException,
            NoID3TagException, IOException {
        doReturn(true).when(spy).hasV2andForcesV1asSource(PRESET_BOOLEAN);
        doNothing().when(spy).addFrames();
        doNothing().when(spy).removeV1tag();
        doNothing().when(spy).updateV2tag();
        spy.convertId3v1toId3v2Tag(PRESET_BOOLEAN);
        verify(spy, times(2)).info(any(String.class));
    }

    @Test
    void testDebug() {
        classUnderTest.setDebug(true);
        classUnderTest.debug(RANDOM_STRING);
    }

    @Test
    void testGetId3v1Tag() {
        classUnderTest.getId3v1Tag(mockFile);
    }

    @Test
    void testGetId3v2Tag() throws ID3v2IllegalVersionException, ID3v2WrongCRCException,
            ID3v2DecompressionException, IOException {
        File file = File.createTempFile(RANDOM_FILE_NAME, RANDOM_FILE_EXTENSION);
        file.deleteOnExit();
        classUnderTest.getId3v2Tag(file);
    }

    @Test
    void testGetUnicodeLittleByteArrayOf() throws UnsupportedEncodingException {
        byte[] expected = PRESET_STRING.getBytes("UnicodeLittle");
        assertThat(classUnderTest.getUnicodeLittleByteArrayOf(PRESET_STRING))
                .isEqualTo(expected);
    }

    @Test
    void testHasV1andDoesNotHaveV2_returns_false_() {
        assertThat(classUnderTest.hasV1andDoesNotHaveV2(false, false)).isFalse();
    }

    @Test
    void testHasV1andDoesNotHaveV2_returns_false_when_hasv1_is_false() {
        assertThat(classUnderTest.hasV1andDoesNotHaveV2(false, true)).isFalse();
    }

    @Test
    void testHasV1andDoesNotHaveV2_returns_false_when_hasv1_is_true_and_hasv2_is_true() {
        assertThat(classUnderTest.hasV1andDoesNotHaveV2(true, true)).isFalse();
    }

    @Test
    void testHasV1andDoesNotHaveV2_returns_true_when_hasv1_is_true() {
        assertThat(classUnderTest.hasV1andDoesNotHaveV2(true, false)).isTrue();
    }

    @Test
    void testHasV1andForcesV1_returns_false() {
        classUnderTest.setForceV1asSource(false);
        assertThat(classUnderTest.hasV1andForcesV1asSource(false)).isFalse();
    }

    @Test
    void testHasV1andForcesV1_returns_false_when_forceV1_is_false() {
        classUnderTest.setForceV1asSource(false);
        assertThat(classUnderTest.hasV1andForcesV1asSource(true)).isFalse();
    }

    @Test
    void testHasV1andForcesV1_returns_false_when_hasV1_is_false() {
        classUnderTest.setForceV1asSource(true);
        assertThat(classUnderTest.hasV1andForcesV1asSource(false)).isFalse();
    }

    @Test
    void testHasV1andForcesV1_returns_true() {
        classUnderTest.setForceV1asSource(true);
        assertThat(classUnderTest.hasV1andForcesV1asSource(true)).isTrue();
    }

    @Test
    void testHasV2andV1isForced_returns_false() {
        classUnderTest.setForceV1asSource(false);
        assertThat(classUnderTest.hasV2andForcesV1asSource(false)).isFalse();
    }

    @Test
    void testHasV2andV1isForced_returns_false_when_forceV1_is_false() {
        classUnderTest.setForceV1asSource(false);
        assertThat(classUnderTest.hasV2andForcesV1asSource(true)).isFalse();
    }

    @Test
    void testHasV2andV1isForced_returns_false_when_hasv2_is_false() {
        classUnderTest.setForceV1asSource(true);
        assertThat(classUnderTest.hasV2andForcesV1asSource(false)).isFalse();
    }

    @Test
    void testHasV2andV1isForced_returns_true() {
        classUnderTest.setForceV1asSource(true);
        assertThat(classUnderTest.hasV2andForcesV1asSource(true)).isTrue();
    }

    @Test
    void testHasV2tagAndV1tagIsNotForced_returns_false() {
        classUnderTest.setForceV1asSource(false);
        assertThat(classUnderTest.hasV2tagAndV1tagIsNotForcedAsSource(false)).isFalse();
    }

    @Test
    void testHasV2tagAndV1tagIsNotForced_returns_false_when_hasv2_is_false_and_forceV1_is_true() {
        classUnderTest.setForceV1asSource(true);
        assertThat(classUnderTest.hasV2tagAndV1tagIsNotForcedAsSource(false)).isFalse();
    }

    @Test
    void testHasV2tagAndV1tagIsNotForced_returns_false_when_hasv2_is_true_and_forceV1_is_true() {
        classUnderTest.setForceV1asSource(true);
        assertThat(classUnderTest.hasV2tagAndV1tagIsNotForcedAsSource(true)).isFalse();
    }

    @Test
    void testHasV2tagAndV1tagIsNotForced_returns_true_when_hasv2_is_true_and_forceV1_is_false() {
        classUnderTest.setForceV1asSource(false);
        assertThat(classUnderTest.hasV2tagAndV1tagIsNotForcedAsSource(true)).isTrue();
    }

    @Test
    void testHasV2Tags_returns_false_when_no_v2_tags() throws NoID3v2TagException {
        spy.setId3v2(mockId3V2Tag);
        when(mockId3V2Tag.hasTag()).thenReturn(false);
        doReturn(false).when(spy).hasV2tagAndV1tagIsNotForcedAsSource(false);
        assertThat(spy.hasV2Tags()).isFalse();
    }

    @Test
    void testHasV2Tags_returns_false_when_v2_frames_are_not_present()
            throws NoID3v2TagException {
        spy.setId3v2(mockId3V2Tag);
        when(mockId3V2Tag.hasTag()).thenReturn(true);
        doReturn(true).when(spy).hasV2tagAndV1tagIsNotForcedAsSource(true);
        when(mockId3V2Tag.getFrames()).thenThrow(new NoID3v2TagException());
        assertThat(spy.hasV2Tags()).isFalse();
    }

    @Test
    void testHasV2Tags_returns_true_when_v2_frames_are_present()
            throws NoID3v2TagException {
        spy.setId3v2(mockId3V2Tag);
        when(mockId3V2Tag.hasTag()).thenReturn(true);
        doReturn(true).when(spy).hasV2tagAndV1tagIsNotForcedAsSource(true);
        when(mockId3V2Tag.getFrames()).thenReturn(mockFrames);
        assertThat(spy.hasV2Tags()).isTrue();
    }

    @Test
    void testInfo() {
        classUnderTest.setQuiet(true);
        classUnderTest.info(RANDOM_STRING);
    }

    @Test
    void testIsNumericalStringOrUrl_returns_false_when_version_is_3_but_id_is_null() {
        spy.setId3v2(mockId3V2Tag);
        when(mockId3V2Tag.getVersion()).thenReturn(THREE);
        assertThat(classUnderTest.isNumericalStringOrUrl(mockId3V2Frame)).isFalse();
    }

    @Test
    void testIsNumericalStringOrUrl_returns_false_when_version_is_not_3() {
        spy.setId3v2(mockId3V2Tag);
        when(mockId3V2Tag.getVersion()).thenReturn(NOT_3);
        assertThat(classUnderTest.isNumericalStringOrUrl(mockId3V2Frame)).isFalse();
    }

    @Test
    void testIsNumericalStringOrUrl_returns_false_when_version_is_null() {
        assertThat(classUnderTest.isNumericalStringOrUrl(mockId3V2Frame)).isFalse();
    }

    @Test
    void testIsNumericalStringOrUrl_returns_true_when_version_is_3_and_is_nonunicodefield() {
        spy.setId3v2(mockId3V2Tag);
        when(mockId3V2Tag.getVersion()).thenReturn(THREE);
        when(mockId3V2Frame.getID()).thenReturn("TDAT");
        assertThat(classUnderTest.isNumericalStringOrUrl(mockId3V2Frame)).isTrue();
    }

    @Test
    void testIsText_returns_false_when_id_does_not_start_with_capital_letter_t() {
        when(mockId3V2Frame.getID()).thenReturn("t");
        assertThat(classUnderTest.isText(mockId3V2Frame)).isFalse();
    }

    @Test
    void testIsText_returns_true_when_id_starts_with_capital_letter_t() {
        when(mockId3V2Frame.getID()).thenReturn("T");
        assertThat(classUnderTest.isText(mockId3V2Frame)).isTrue();
    }

    @Test
    void testReencode_does_not_reencode_when_not_needed() throws UnsupportedEncodingException {
        when(mockId3V2Frame.getContent()).thenReturn(PRESET_BYTE_ARRAY);
        doReturn(false).when(spy).shouldBeReenconded(PRESET_BYTE_ARRAY);
        assertThat(spy.reencode(mockId3V2Frame)).isFalse();
    }

    @Test
    void testReencode_reencodes_when_needed() throws UnsupportedEncodingException {
        when(mockId3V2Frame.getContent()).thenReturn(PRESET_BYTE_ARRAY);
        doReturn(true).when(spy).shouldBeReenconded(PRESET_BYTE_ARRAY);
        spy.reencode(mockId3V2Frame);
        assertThat(spy.reencode(mockId3V2Frame)).isTrue();
    }

    @Test
    void testReencodeAndUpdate_does_not_update_if_last_frame_not_reencoded()
            throws UnsupportedEncodingException, IOException,
            ID3v2DecompressionException {
        /*
         * This test illustrates a potential bug. If the last frame in the loop is not
         * re-encoded, then the v2 tag would not be updated
         */
        spy.setId3v2(mockId3V2Tag);
        Vector<ID3v2Frame> frames = new Vector();
        ID3v2Frame frame1 = new ID3v2Frame(mockInputStream);
        ID3v2Frame frame2 = new ID3v2Frame(mockInputStream);
        frames.add(frame1);
        frames.add(frame2);
        doReturn(true).when(spy).reencodeFrame(frame1);
        doReturn(false).when(spy).reencodeFrame(frame2);
        spy.reencodeAndUpdate(frames);
        verify(mockId3V2Tag, never()).touch();
        verify(mockId3V2Tag, never()).update();
        verify(spy, times(1)).removeV1tag();
    }

    @Test
    void testReencodeAndUpdate_does_update_if_intermediate_frame_not_reencoded()
            throws UnsupportedEncodingException, IOException,
            ID3v2DecompressionException {
        spy.setId3v2(mockId3V2Tag);
        Vector<ID3v2Frame> frames = new Vector();
        ID3v2Frame frame1 = new ID3v2Frame(mockInputStream);
        ID3v2Frame frame2 = new ID3v2Frame(mockInputStream);
        ID3v2Frame frame3 = new ID3v2Frame(mockInputStream);
        frames.add(frame1);
        frames.add(frame2);
        frames.add(frame3);
        doReturn(true).when(spy).reencodeFrame(frame1);
        doReturn(false).when(spy).reencodeFrame(frame2);
        doReturn(true).when(spy).reencodeFrame(frame3);
        spy.reencodeAndUpdate(frames);
        verify(mockId3V2Tag, times(1)).touch();
        verify(mockId3V2Tag, times(1)).update();
        verify(spy, times(1)).removeV1tag();
    }

    @Test
    void testReencodeAndUpdate_happy_path() throws UnsupportedEncodingException,
            IOException, ID3v2DecompressionException {
        spy.setId3v2(mockId3V2Tag);
        Vector<ID3v2Frame> frames = new Vector();
        ID3v2Frame frame1 = new ID3v2Frame(mockInputStream);
        frames.add(frame1);
        doReturn(true).when(spy).reencodeFrame(frame1);
        spy.reencodeAndUpdate(frames);
        verify(mockId3V2Tag, times(1)).touch();
        verify(mockId3V2Tag, times(1)).update();
        verify(spy, times(1)).removeV1tag();
    }

    @Test
    void testReencodeFrame_reencodes_text_frame() throws UnsupportedEncodingException {
        doReturn(true).when(spy).isText(mockId3V2Frame);
        doReturn(false).when(spy).isNumericalStringOrUrl(mockId3V2Frame);
        doReturn(true).when(spy).reencode(mockId3V2Frame);
        spy.reencodeFrame(mockId3V2Frame);
        verify(spy, times(1)).reencode(mockId3V2Frame);
    }

    @Test
    void testReencodeFrame_returns_false_when_frame_is_a_numerical_string_or_a_url()
            throws UnsupportedEncodingException {
        doReturn(true).when(spy).isText(mockId3V2Frame);
        doReturn(true).when(spy).isNumericalStringOrUrl(mockId3V2Frame);
        assertThat(spy.reencodeFrame(mockId3V2Frame)).isFalse();
    }

    @Test
    void testReencodeFrame_returns_false_when_frame_is_not_text()
            throws UnsupportedEncodingException {
        doReturn(false).when(spy).isText(mockId3V2Frame);
        assertThat(spy.reencodeFrame(mockId3V2Frame)).isFalse();
    }

    @Test
    void testReencodeId3v2TagToUnicode_does_not_reencode_if_no_frames()
            throws NoID3v2TagException, UnsupportedEncodingException, IOException {
        spy.setId3v2(mockId3V2Tag);
        when(mockId3V2Tag.getFrames()).thenReturn(null);
        spy.reencodeId3v2TagToUnicode();
        verify(spy, never()).reencodeAndUpdate(any(Vector.class));
    }

    @Test
    void testReencodeId3v2TagToUnicode_reencodes_if_there_are_frames()
            throws NoID3v2TagException, UnsupportedEncodingException, IOException {
        spy.setId3v2(mockId3V2Tag);
        when(mockId3V2Tag.getFrames()).thenReturn(mockFrames);
        doReturn(true).when(spy).thereAreFrames(mockFrames);
        doNothing().when(spy).reencodeAndUpdate(mockFrames);
        spy.reencodeId3v2TagToUnicode();
        verify(spy, times(1)).reencodeAndUpdate(mockFrames);
    }

    @Test
    void testRemoveV1tag_does_not_remove_tag_when_dry_and_removeV1_are_false_by_default()
            throws IOException {
        classUnderTest.setId3v1(mockId3V1Tag);
        classUnderTest.removeV1tag();
        verify(mockId3V1Tag, never()).removeTag();
    }

    @Test
    void testRemoveV1tag_does_not_remove_tag_when_dry_and_removeV1_are_true()
            throws IOException {
        classUnderTest.setId3v1(mockId3V1Tag);
        classUnderTest.setDry(true);
        classUnderTest.setRemoveV1(true);
        classUnderTest.removeV1tag();
        verify(mockId3V1Tag, never()).removeTag();
    }

    @Test
    void testRemoveV1tag_removes_tag_when_dry_is_false_and_removeV1_is_true()
            throws IOException {
        classUnderTest.setId3v1(mockId3V1Tag);
        classUnderTest.setDry(false);
        classUnderTest.setRemoveV1(true);
        classUnderTest.removeV1tag();
        verify(mockId3V1Tag, times(1)).removeTag();
    }
    
    @Test
    void testShouldBeReenconded_returns_false_if_byte_array_does_not_start_with_zero()
            throws UnsupportedEncodingException {
        byte[] byteArrayWithLeadingZero = new byte[] { NOT_ZERO, RANDOM_BYTE };
        assertThat(classUnderTest.shouldBeReenconded(byteArrayWithLeadingZero)).isFalse();
    }
    
    @Test
    void testShouldBeReenconded_returns_false_if_byte_array_starts_with_zero_but_is_less_than_2_bytes_long()
            throws UnsupportedEncodingException {
        byte[] byteArrayWithLeadingZero = new byte[] { ZERO };
        assertThat(classUnderTest.shouldBeReenconded(byteArrayWithLeadingZero)).isFalse();
    }

    @Test
    void testShouldBeReenconded_returns_true_if_byte_array_starts_with_zero_and_more_than_one_byte_long()
            throws UnsupportedEncodingException {
        byte[] byteArrayWithLeadingZero = new byte[] { ZERO, RANDOM_BYTE };
        assertThat(classUnderTest.shouldBeReenconded(byteArrayWithLeadingZero)).isTrue();
    }

    @Test
    void testShouldForceV1toV2TagConversion_returns_false() {
        doReturn(false).when(spy).hasV1andDoesNotHaveV2(any(Boolean.class),
                any(Boolean.class));
        doReturn(false).when(spy).hasV1andForcesV1asSource(any(Boolean.class));
        final boolean RANDOM_HASV1 = false;
        final boolean RANDOM_HASV2 = false;
        assertThat(spy.shouldForceV1toV2TagConversion(RANDOM_HASV1, RANDOM_HASV2))
                .isFalse();
    }

    @Test
    void testShouldForceV1toV2TagConversion_returns_true_when_hasV1() {
        doReturn(true).when(spy).hasV1andDoesNotHaveV2(any(Boolean.class),
                any(Boolean.class));
        final boolean RANDOM_HASV1 = false;
        final boolean RANDOM_HASV2 = false;
        assertThat(spy.shouldForceV1toV2TagConversion(RANDOM_HASV1, RANDOM_HASV2))
                .isTrue();
    }

    @Test
    void testShouldForceV1toV2TagConversion_returns_true_when_hasV1andForcesV1() {
        doReturn(false).when(spy).hasV1andDoesNotHaveV2(any(Boolean.class),
                any(Boolean.class));
        doReturn(true).when(spy).hasV1andForcesV1asSource(any(Boolean.class));
        final boolean RANDOM_HASV1 = false;
        final boolean RANDOM_HASV2 = false;
        assertThat(spy.shouldForceV1toV2TagConversion(RANDOM_HASV1, RANDOM_HASV2))
                .isTrue();
    }

    @Test
    void testShouldId3V2BeUpdated_returns_true_when_dry_is_defaulted_and_isUpdated_is_false() {
        assertThat(classUnderTest.shouldId3V2BeUpdated(false)).isFalse();
    }

    @Test
    void testShouldId3V2BeUpdated_returns_true_when_dry_is_defaulted_and_isUpdated_is_true() {
        assertThat(classUnderTest.shouldId3V2BeUpdated(true)).isTrue();
    }

    @Test
    void testShouldId3V2BeUpdated_returns_true_when_dry_is_true_and_isUpdated_is_true() {
        classUnderTest.setDry(true);
        assertThat(classUnderTest.shouldId3V2BeUpdated(true)).isFalse();
    }

    @Test
    void testThereAreFrames_returns_false_when_frames_are_not_null_but_are_empty() {
        assertThat(classUnderTest.thereAreFrames(mockFrames)).isFalse();
    }

    @Test
    void testThereAreFrames_returns_false_when_frames_are_null() {
        assertThat(classUnderTest.thereAreFrames(null)).isFalse();
    }

    @Test
    void testThereAreFrames_returns_true_when_frames_are_not_empty()
            throws ID3v2DecompressionException, IOException {
        Vector<ID3v2Frame> frames = new Vector();
        ID3v2Frame frame1 = new ID3v2Frame(mockInputStream);
        frames.add(frame1);
        assertThat(classUnderTest.thereAreFrames(frames)).isTrue();
    }

    @Test
    void testUpdateV2tag_does_not_update_tag_on_dry_run() throws IOException {
        spy.setId3v2(mockId3V2Tag);
        classUnderTest.setDry(true);
        classUnderTest.updateV2tag();
        verify(mockId3V2Tag, never()).update();
    }

    @Test
    void testUpdateV2tag_updates_tag() throws IOException {
        spy.setId3v2(mockId3V2Tag);
        classUnderTest.updateV2tag();
        verify(mockId3V2Tag, times(1)).update();
    }
}
