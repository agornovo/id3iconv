package net.zhoufeng;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.google.common.io.Files;

@ExtendWith(SpringExtension.class)
class IntegrationTest {
    private ID3iconv classUnderTest;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        classUnderTest = new ID3iconv();
        classUnderTest.setConverter(new Converter());
    }

    @Test
    void testRun_converts_id3v1_tag_to_unicode() throws IOException, URISyntaxException {
        Path tempConvertedInPlaceFilePath = tempDir.resolve("sample-id3v1-unconverted.mp3");
        FileUtils.copyFile(new File("src/test/resources/sample-id3v1-unconverted.mp3"),
                tempConvertedInPlaceFilePath.toFile());
        String[] args = { "-e", "WINDOWS-1251", "-d", "-v1",
                tempConvertedInPlaceFilePath.toString() };
        classUnderTest.run(args);
        File actual = tempConvertedInPlaceFilePath.toFile();
        File expected = new File("src/test/resources/sample-id3v1-converted.mp3");
        assertThat(Files.equal(actual, expected)).isTrue();
    }

    @Test
    void testRun_converts_id3v2_tag_to_unicode() throws IOException, URISyntaxException {
        Path tempConvertedInPlaceFilePath = tempDir.resolve("sample-id3v2-unconverted.mp3");
        FileUtils.copyFile(new File("src/test/resources/sample-id3v2-unconverted.mp3"),
                tempConvertedInPlaceFilePath.toFile());
        String[] args = { "-e", "WINDOWS-1251", "-d",
                tempConvertedInPlaceFilePath.toString() };
        classUnderTest.run(args);
        File actual = tempConvertedInPlaceFilePath.toFile();
        File expected = new File("src/test/resources/sample-id3v2-converted.mp3");
        assertThat(Files.equal(actual, expected)).isTrue();
    }

}
