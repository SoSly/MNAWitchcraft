# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased](https://github.com/SoSly/MNAWitchcraft/tree/1.20.1)
### Added
- added Coven progression spell effect tracking system for tiers 3-5
- added `/mnaw progress <player>` command to view coven progression status

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
