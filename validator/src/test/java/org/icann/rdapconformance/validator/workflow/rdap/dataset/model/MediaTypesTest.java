package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import org.assertj.core.api.Assertions;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class MediaTypesTest extends BaseUnmarshallingTest<MediaTypes> {

    private MediaTypes mediaTypes;

    @BeforeMethod
    public void setUp() throws IOException, ParserConfigurationException, SAXException {
        this.mediaTypes = unmarshal("/dataset/media-types.xml", MediaTypes.class);
    }

    @Test
    public void givenValidMediaTypeXml_whenUnmarshalling_thenReturnMediaType() {
        Assertions.assertThat(mediaTypes.getRecords())
                .contains("BMPEG",
                        "MP1S",
                        "BT656",
                        "SMPTE292M",
                        "DV",
                        "MPV",
                        "AV1",
                        "MP4V-ES",
                        "VP8",
                        "CelB",
                        "H263-2000",
                        "JPEG",
                        "H263-1998",
                        "MP2T",
                        "vnd.sealedmedia.softseal.mov",
                        "vnd.nokia.interleaved-multimedia",
                        "MP2P");
    }

}