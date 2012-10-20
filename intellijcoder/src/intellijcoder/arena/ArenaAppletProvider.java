package intellijcoder.arena;

import intellijcoder.main.IntelliJCoderException;
import intellijcoder.os.Network;

import java.io.IOException;
import java.io.InputStream;

public class ArenaAppletProvider {
    public static final String TOPCODER_JNLP_URL = "http://www.topcoder.com/contest/arena/ContestAppletProd.jnlp";
    private final Network network;
    private final ArenaFileParser fileParser;
    private ArenaJarDownloader jarDownloader;

    public ArenaAppletProvider(Network network, ArenaFileParser fileParser, ArenaJarDownloader jarDownloader) {
        this.network = network;
        this.fileParser = fileParser;
        this.jarDownloader = jarDownloader;
    }

    public ArenaAppletInfo getApplet() throws IntelliJCoderException {
        try {
            InputStream inputStream = network.getUrlInputStream(TOPCODER_JNLP_URL);
            ArenaAppletInfo appletInfo = fileParser.parse(inputStream);
            return jarDownloader.loadArenaJars(appletInfo);
        } catch (IOException e) {
            throw new IntelliJCoderException("Failed to load jnlp file", e);
        }
    }
}
