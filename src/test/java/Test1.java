/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import org.junit.jupiter.api.Test;

import com.github.hyperZebra.PCARD;


/**
 * Test1.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/03/07 umjammer initial version <br>
 */
class Test1 {

    @Test
    void test1() throws Exception {
//        PCARD.main(new String[] { "src/test/resources/zizi vol.16" });
        PCARD.main(new String[] { "src/test/resources/What is HyperCard?" });
        Thread.sleep(300000);
    }
}

/* */
