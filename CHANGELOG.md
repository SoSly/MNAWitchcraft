# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Fixed
- fixed spell requirements and completion progress resetting on player death/respawn
- fixed faction-locked spells appearing as coven advancement requirements, making progression impossible without extensive trading
- fixed Witch's Cauldron not refilling with moonlight after being emptied with bucket (only affected bucket interaction, not bottles)
- fixed Witch's Cauldron continuing to heat from extinguished campfires (now properly checks campfire lit state)

### Added
- added soul campfire support as heat source for Witch's Cauldron

## [1.20.1-forge-0.3.1-alpha]
### Added
- tier requirements now generate when players carry scented M&A flowers, making progression accessible through witch gossip alone
- added "Sworn to the Hedge" advancement for joining the Coven faction

### Changed
- improved "Ritual: Sympathy" guidebook entry to explain how to obtain bloody needles for bound poppet creation

### Fixed
- fixed crash when crafting bound poppet with bloody needle lacking proper NBT data (such as from /give command)
- fixed "Swept Away" advancement triggering on any inventory change instead of only when obtaining flying broom
- fixed "Swept Away" advancement appearing in its own category instead of under M&A tier progression
- fixed Mage Robe Armor regeneration bonus incorrectly affecting Coven players (witches cannot regenerate essence naturally)

## [1.20.1-forge-0.3.0-alpha](https://github.com/SoSly/MnAWitchcraft/releases/tag/1.20.1-forge-0.3.0-alpha)
### Added
- added Flying Broom, a customizable rideable entity that allows flight in the overworld and nether
- added Coven progression spell effect tracking system for tiers 3-5
- added `/mnaw progress <player>` command to view coven progression status
- added `/mnaw progress <player> complete` command to mark current tier progression as complete
- added Witch Gossip discovery system for organic Coven progression hints
- added "You smell nice" potion effect for players carrying M&A flowers
- added configurable witch gossip mechanics (cooldown, distance, spell hint chance)
- added Ritual of the Hedge for Coven faction joining and tier advancement
- added Moonthread Armor, a tier 5 Coven armor set with unique protective abilities
- added Book of Shadows, the faction grimoire for Coven mages (model and texture by Aranai)
- added Condensed Moonlight, a magical fluid that serves as a premium brewing ingredient
- added Condensed Moonlight Bucket for transporting moonlight
- added Bottle of Condensed Moonlight for brewing and crafting
- added Condensed Moonlight Cauldron that emits a soft glow when filled
- added Moonthread item, an enhanced thread infused with moonlight for Coven crafting
- added manaweaving recipe to create Moonthread from Condensed Moonlight and Infused Thread
- added Witch's Cauldron, an enhanced crucible for Coven witches that passively collects moonlight at night

### Changed
- migrated configuration from client-side to server-side (per-world configuration)

### Fixed
- fixed z-fighting issue with Coven armor left shoulder by correcting pivot point alignment
- fixed compatibility issues with latest Mana and Artifice versions

## [1.20.1-forge-0.2.1-alpha](https://github.com/SoSly/MnAWitchcraft/releases/tag/1.20.1-forge-0.2.1-alpha)
### Fixed
- removed a naughty comma from the potion pouch guide entry which was causing codex crashes

## [1.20.1-forge-0.2.0-alpha](https://github.com/SoSly/MnAWitchcraft/releases/tag/1.20.1-forge-0.2.0-alpha)
### Requirements
- [Mystic Alchemy](https://www.curseforge.com/minecraft/mc-mods/mystic-alchemy) is now **required**.

### Added
- added a new coven-only "dedication" enchantment that adds charges to custom staves, wands, and bangles
- added the witch eye, a necklace that allows the wearer to understand the alchemical properties of items (requires Mystic Alchemy)
- added a new potion amulet that allows the wearer to gain immunity to a potion effect while it is worn
- transmuted silver can now be broken down into nuggets (and I suppose you can make them into ingots again, but why would you want to do that?)
- added a new potion pouch that can store stacks of potions 
- added 4 textures, one for each piece of the Witch's Armor

### Changed
- the bound poppet now also counts as a player charm
- bloody needles and bound poppets now display who they are bound to in their tooltip 
- dedicated items can be recharged by right-clicking a crucible prepared with instant mana or mana regen effects
- dedicated items will resist expending their last charge
- added comprehensive logging throughout the mod for better error tracking and debugging

### Fixed
- fixed a typo in the factionless mages codex entry
- fixed a bug causing manaweaving to crash the game after joining a coven
- fixed a bug causing the occulus to crash the game when all progression steps were complete
- poppets and bound poppets can now be picked up after they have been placed

## [1.20.1-forge-0.1.0-alpha](https://github.com/SoSly/MnAWitchcraft/releases/tag/1.20.1-forge-0.1.0-alpha)
### Added
- you can now use a vinteum needle to collect blood from an unwilling victim
- you can wash a bloody vinteum needle to remove the blood
- bloody vinteum needles now turn red (thanks Aranai!)
- you can now craft a poppet from wool and animus dust; they're cute l'il guys
- you can combine a bloody needle and a poppet to create a bound poppet
- you can now use the ritual of sympathy to cast spells on the target of a bound poppet
- a new configuration has been added to block sympathetic magic in boss arenas
- a new configuration has been added to prevent sympathetic magic from targeting bosses
- a new configuration has been added to set the number of spelleffects required to progress as a witch
- the coven and dark coven factions have been added to the game
- a new charm has been added to protect the wearer from sympathetic magic
- you can now use the ritual of broken sympathy to destroy a bound poppet
- added an entry about factionless mages to the codex

### Changed
- players now hold the vinteum needle in a more appropriate manner for stabbing people
