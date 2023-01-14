package net.zhoufeng;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.google.common.io.Files;

@ExtendWith(SpringExtension.class)
class IntegrationTest {
    private ID3iconv classUnderTest;

    @BeforeEach
    void setUp() {
        classUnderTest = new ID3iconv();
        classUnderTest.setConverter(new Converter());
    }

    @Test
    void testRun() throws IOException, URISyntaxException {
        /*
         * All content in src/test/resources is copied into target/test-classes folder.
         * So to get file from test resources during maven build you have to load it
         * from test-classes folder
         * 
         * https://stackoverflow.com/a/50187427
         */
        Path sampleFilePath = Paths.get(
                getClass().getProtectionDomain().getCodeSource().getLocation().toURI())
                .resolve(Paths.get("sample-uncoverted.mp3"));
        String[] args = { "-e", "WINDOWS-1251", "-d", sampleFilePath.toString() };
        classUnderTest.run(args);
        File convertedInPlaceFile = sampleFilePath.toFile();
        File expectedFile = new File("src/test/resources/sample-converted.mp3");
        assertThat(Files.equal(convertedInPlaceFile, expectedFile)).isTrue();
    }

}
