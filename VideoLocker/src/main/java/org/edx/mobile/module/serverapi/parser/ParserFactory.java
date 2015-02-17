package org.edx.mobile.module.serverapi.parser;

public class ParserFactory {
	
    private static IParser instance;

    /**
     * Returns singleton instance of {@link IParser} interface.
     * @return
     */
    public static IParser getInstance() {
        if (instance == null) {
            instance = new GsonParser();
        }
        return instance;
    }
}
