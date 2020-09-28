## Tables in gameData

### user

Represents a user of this service, who might be a player or admin for any number of games.

- **id**: the ID of the user (from loginData)

The IDs do not autoincrement: when adding a user, the ID from loginData is manually input here as well. (You might need to use `select last_insert_rowid()` to get the ID of a user added to loginData.) There is a one-to-one correspondence between users in loginData and users in this table.

### game

Stores information for game creation and tracking game status. 

- **id**: the internal game ID, an auto incrementing integer
- **status**: 0 if the game is forming, 1 if the game is playing, 2 if the game is finished (outside of the database, this is represented with the GameStatus enum)
- **name**: the name of the game
- **rules**: the rules of the game (Markdown)
- **max_team_size**: the maximum allowed team size
- **created**: the time the game was created, as a Unix timestamp
- **join code**: the code to join the game
- **num_targets**: the maximum number of targets per team
- **anonymous**: whether players can see which players they are targeting (0) or just the codenames of the target teams (1)
- **current_round**: the current round number. (Rounds have not been implemented)

### admin

Stores game admins. A user ID can be in the user column multiple times if that user is the admin of multiple games. A game ID can be in the game column multiple times if that game has multiple admins.

- **user**: the user ID of the admin
- **game**: the ID of the game they are an admin of

### team

Stores information for tracking the status of a team for an individual game.

- **id**: the internal team ID, an auto incrementing integer
- **codename**: the team’s codename
- **game**: the ID of the game the team is part of
- **alive**: 1 if this team is alive, 0 if it has been killed (i.e. all its members have been killed)
- **join_code**: the code to join the team

### player

Stores players in games. One user can be associated with multiple players if they are playing multiple games.

- **user**: the ID of the user whose account is associated with this player
- **team**: the ID of the team the player is on
- **id**: the internal player ID, an auto incrementing integer
- **kill_code**: the code to kill the player
- **alive**: 1 if the player is alive, 0 if the player is dead
- **joined_team**: the time the player joined the team, as a Unix timestamp

(The player table doesn’t have a game column because that information is already stored in the team.)

### target

Stores assassin/target pairs. Each row represents one assassin/target relationship. One team can be represented in either column multiple times. The number of times a team is in the killer column is the number of teams that team is targeting and the number of times a team is in the target column is the number of teams targeting it.

- **killer**: the ID of the assassin team
- **target**: the ID of the target team

### kill

Records who killed whom and when.

- **killer**: the ID of the player who killed
- **target**: the ID of the player who was killed
- **kill_time**: the time the kill happened, as a Unix timestamp
- **round**: the ID of the round during which the kill happened. (Rounds have not been implemented)
- **eliminated_team**: whether this kill also eliminated the target’s team

### message

Records messages in game message feeds. The actual text content is generated from GUI code and fields in the message_field table; this table just records the types of messages and when they were sent.

- **id**: the internal message ID, an auto incementing integer
- **time**: the time the message was sent, as a Unix timestamp
- **game**: the ID of the game in which the message was sent
- **type**: the type of the message, as a text keyword. Can be any of the following: start, win, end, change name, change rules, change anon, change not anon, change num targets, new targets, revive, eliminate, surrender, custom, none

### message_field

Stores the text content that is specific to particular messages, rather than general to all messages of one type. (For example, the codenames of teams in elimination messaegs.)

- **message**: the ID of the message this field is associated with
- **field_index**: the index of the field within the message, indexed from 0 (whether this is the first field, second, etc). This together with message ID form the primary key.
- **content**: the text content of this field

### round

Represents a round (not yet implemented).

- **game**: the ID of the game the round is part of
- **number**: the number of the round within the game (this combined with the game ID form the primary key)
- **new_targets**: 1 if new targets are assigned during this round, 0 if they aren’t
- **start**: the start time of this round
- **end**: the end time of this round (can be null if there’s no scheduled end time)