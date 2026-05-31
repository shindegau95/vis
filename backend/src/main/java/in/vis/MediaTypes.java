package in.vis;

import org.springframework.http.MediaType;

public final class MediaTypes {

    public static final String APPLICATION_VND_VIS_V1_JSON_VALUE = "application/vnd.vis.v1+json";
    public static final String APPLICATION_VND_VIS_V2_JSON_VALUE = "application/vnd.vis.v2+json";

    public static final MediaType APPLICATION_VND_VIS_V1_JSON = MediaType.valueOf(APPLICATION_VND_VIS_V1_JSON_VALUE);
    public static final MediaType APPLICATION_VND_VIS_V2_JSON = MediaType.valueOf(APPLICATION_VND_VIS_V2_JSON_VALUE);

    private MediaTypes() {}
}
