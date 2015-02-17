package org.edx.mobile.module.serverapi.parser;

import java.io.Reader;
import java.util.List;

public interface IParser {

	public <T> T parseObject(String data, Class<T> cls) throws Exception;
    public <T> T parseObject(Reader reader, Class<T> cls) throws Exception;
	public <T> List<T> parseList(String data, Class<T> cls) throws Exception;
}
