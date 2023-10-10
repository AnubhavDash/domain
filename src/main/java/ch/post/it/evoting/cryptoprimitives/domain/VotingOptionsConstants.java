/*
 * Copyright 2022 Post CH Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.post.it.evoting.cryptoprimitives.domain;

public class VotingOptionsConstants {

	public static final int MAXIMUM_NUMBER_OF_VOTING_OPTIONS = 5000;
	public static final int MAXIMUM_NUMBER_OF_SELECTABLE_VOTING_OPTIONS = 120;
	public static final int MAXIMUM_NUMBER_OF_WRITE_IN_OPTIONS = 15;
	public static final int MAXIMUM_WRITE_IN_OPTION_LENGTH = 500;
	public static final int MAXIMUM_ACTUAL_VOTING_OPTION_LENGTH = 50;

	private VotingOptionsConstants() {
		// This class should not be instantiated
	}
}
