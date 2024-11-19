# Simple Groups
Simple Groups is a 1.21.3 Fabric mod to allow players to create their own groups. 
This is my first attempt at making a Fabric mod, so a lot of the code isn't amazing. I'd like to make a v2 version of this mod, but for now I'd like to focus on other projects.

Requires Fabric API. Needs to be installed both server-side and client-side.


# Commands
`/group config color <r> <g> <b>` - Changes the color of the group. Appears in the group member's display names.

`/group config joinOptions <byInviteOnly|anyoneCanRequest|anyoneCanJoin>` - Changes how new members can join the group. `byInviteOnly` allows players to join if they are invited to the group by the leader. `anyoneCanRequest` allows players to join if they are invited, or if they request to join and are accepted. Join requests are saved even after server reboot/shutdown. `anyoneCanJoin` allows anyone to join the group.

`/group config name <name>` - Changes the name of the group.

`/group config prefix <prefix>` - Changes the prefix of the group. Appears in the group member's display names.

`/group create <name>` - Creates a new group.

`/group delete` (leader-only command) - Deletes the group. Must be run twice within 15 seconds to confirm the deletion.

`/group invite <player>` (leader-only command) - Invites a player to the group. They must accept for the entire session of the server. Invites are not kept after server reboots/shutdowns, though this will likely be implemented in the future.

`/group join <groupName>` - Joins a group. If the group is invite only, the command will not work. If the group is request/invite only, the command will send a join request to the leader. If the group is open to anyone, the player will immediately join the group.

`/group kick <player>` (leader-only command) - Kicks a player from the group.

`/group leave` - Leaves the current group you're in. If you are the leader of the group, you must transfer ownership before leaving or delete the group.

`/group of <player>` - Gets the name of the group a player is in.

`/group requests view` (leader-only command) - Views all active join requests.

`/group requests accept <player|ALL_JOIN_REQUESTS>` (leader-only command) - If the name of a player is provided, their request will be accepted and they'll be added to the group. If `ALL_JOIN_REQUESTS` is provided, then all join requests will be accepted and all of the players will be added to the group. If players are already in a different group, their requests will be removed and they will not be added to the group.

`/group requests deny <player|ALL_JOIN_REQUESTS>` (leader-only command) - If the name of a player is provided, their request will be denied. If `ALL_JOIN_REQUESTS` is provided, then all join requests will be denied.

`/group transfer <player>` (leader-only command) - Transfers the group to the provided player. The player must be in the group and the leader must run the command twice within 15 seconds to confirm the transfer.
