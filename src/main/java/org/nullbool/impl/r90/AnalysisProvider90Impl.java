/** pi-rs, a generic framework for loading Java Applets in a contained environment.
 * Copyright (C) 2015  NullBool
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.nullbool.impl.r90;

import java.io.IOException;

import org.nullbool.api.Builder;
import org.nullbool.api.Revision;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.impl.analysers.ClientAnalyser;
import org.nullbool.impl.analysers.entity.ActorAnalyser;
import org.nullbool.impl.r82.AnalysisProvider82Impl;
import org.topdank.banalysis.filter.Filter;

/**
 * @author Bibl (don't ban me pls)
 * @created 21 Aug 2015 12:23:32
 */
public class AnalysisProvider90Impl extends AnalysisProvider82Impl {

	public AnalysisProvider90Impl(Revision revision) throws IOException {
		super(revision);
	}
	
	@Override
	public Builder<ClassAnalyser> registerAnalysers() throws AnalysisException {
		return super.registerAnalysers()
				.replace(t -> ActorAnalyser.class.isAssignableFrom(t.getClass()), new ActorAnalyser90())
				.replace(t -> ClientAnalyser.class.isAssignableFrom(t.getClass()), new ClientAnalyser90());
	}
}