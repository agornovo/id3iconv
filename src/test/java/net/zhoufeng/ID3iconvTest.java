package net.zhoufeng;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.vdheide.mp3.ID3v2DecompressionException;
import de.vdheide.mp3.ID3v2IllegalVersionException;
import de.vdheide.mp3.ID3v2WrongCRCException;
import de.vdheide.mp3.NoID3TagException;
import de.vdheide.mp3.NoID3v2TagException;

@ExtendWith(SpringExtension.class)
class ID3iconvTest {

    private static final String PRESET_ENCODING = "ABC";

    @InjectMocks
    private ID3iconv classUnderTest;

    private AutoCloseable closeable;

    @MockBean
    private Converter mockConverter;

    @Mock
    private File mockDirectory;

    @Mock
    private File mockFile;

    @Spy
    @InjectMocks
    ID3iconv spy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testRun_calls_convert_on_file() throws ID3v2IllegalVersionException, ID3v2WrongCRCException,
            ID3v2DecompressionException, NoID3TagException, IOException, NoID3v2TagException {
        String[] presetArgs = { "" };
        doReturn(mockFile).when(spy).getFile(any(String.class));
        when(mockFile.isDirectory()).thenReturn(false);
        spy.run(presetArgs);
        verify(mockConverter, times(1)).convert(mockFile);
    }

    @Test
    void testRun_calls_convert_on_files_in_directory() throws ID3v2IllegalVersionException,
            ID3v2WrongCRCException, ID3v2DecompressionException, NoID3TagException, IOException, NoID3v2TagException {
        String[] presetArgs = { "" };
        doReturn(mockDirectory).when(spy).getFile(any(String.class));
        when(mockDirectory.isDirectory()).thenReturn(true);
        Collection<File> presetFileList = new ArrayList<>();
        presetFileList.add(mockFile);
        doReturn(presetFileList).when(spy).listFiles(mockDirectory, new String[] { "mp3" }, true);
        spy.run(presetArgs);
        verify(mockConverter, times(1)).convert(mockFile);
        verify(spy, times(1)).listFiles(mockDirectory, new String[] { "mp3" }, true);
    }

    @Test
    void testRun_calls_usage_when_no_arguments() {
        String[] args = {};
        spy.run(args);
        verify(spy, times(1)).usage();
    }

    @Test
    void testRun_exits_loop_when_no_switch_is_found() {
        String[] presetArgs = { "" };
        classUnderTest.run(presetArgs);
    }

    @Test
    void testRun_quits_on_unknown_option() {
        String[] presetArgs = { "-" };
        doNothing().when(spy).quit();
        spy.run(presetArgs);
        verify(spy, times(1)).quit();
    }

    @Test
    void testRun_sets_dry_from_p_option() {
        String[] presetArgs = { "-p" };
        classUnderTest.run(presetArgs);
        verify(mockConverter, times(1)).setDry(true);
    }

    @Test
    void testRun_sets_encoding_from_e_option() {
        String[] presetArgs = { "-e", PRESET_ENCODING };
        classUnderTest.run(presetArgs);
        verify(mockConverter, times(1)).setEncoding(PRESET_ENCODING);
    }

    @Test
    void testRun_sets_forcev1_from_v1_option() {
        String[] presetArgs = { "-v1" };
        classUnderTest.run(presetArgs);
        verify(mockConverter, times(1)).setForceV1asSource(true);
    }

    @Test
    void testRun_sets_isDebug_from_d_option() {
        String[] presetArgs = { "-d" };
        classUnderTest.run(presetArgs);
        verify(mockConverter, times(1)).setDebug(true);
    }

    @Test
    void testRun_sets_quiet_from_q_option() {
        String[] presetArgs = { "-q" };
        classUnderTest.run(presetArgs);
        verify(mockConverter, times(1)).setQuiet(true);
    }

    @Test
    void testRun_sets_removev1_from_removev1_option() {
        String[] presetArgs = { "-removev1" };
        classUnderTest.run(presetArgs);
        verify(mockConverter, times(1)).setRemoveV1(true);
    }

}
