package org.edx.mobile.module.serverapi.parser;

public class ParserFactory {
	
    /**
     * Returns new instance of {@link IParser} interface.
     * @return
     */
    public static IParser getInstance() {
        return new GsonParser();
    }
}
