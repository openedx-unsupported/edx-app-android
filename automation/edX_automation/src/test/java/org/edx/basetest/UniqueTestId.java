package org.edx.basetest;

import java.sql.Timestamp;
import java.util.Date;

public class UniqueTestId {
	public String id;

	public UniqueTestId() {
		Date dt = new Date();
		Timestamp ts = new Timestamp(dt.getTime());
		id = ts.toString().replace(":", "").substring(12, 20).replace(".", "");
	}

}
