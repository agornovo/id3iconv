package net.zhoufeng;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class IntegrationTest {
    private ID3iconv classUnderTest;
    
    @BeforeEach
    void setUp() {
        classUnderTest = new ID3iconv();
        classUnderTest.setConverter(new Converter());
    }

    @Test
    void testRun() {
        String[] args = { "-e", "cp1252", "-p", "src/test/resources/sample.mp3" };
        classUnderTest.run(args);
    }
}
