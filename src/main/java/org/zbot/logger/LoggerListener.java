package org.zbot.logger;

import org.zbot.logger.Logger.Message;

/**
 * @author Bibl (don't ban me pls) <br>
 * @created 9 Mar 2015 at 22:17:46 <br>
 */
public abstract interface LoggerListener {

	public abstract void update(Message<?> msg);

	public abstract void clear();
}