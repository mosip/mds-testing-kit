package io.mosip.mds.helper;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExtractDTO {

    private byte[] image;
    private String name;
    private String format;
    private int captureNumber;
}
