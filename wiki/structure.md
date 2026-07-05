# Project Babylon Weapons Structure

## Core layout
- src/main/java/com/rave/projectbabylonweapons/ProjectBabylonWeapons.java
  - Main mod entrypoint
  - Registers Forge reload listeners, networks, registries, animations, and skills
- src/main/java/com/rave/projectbabylonweapons/init/
  - Registries for items, blocks, entities, effects, sounds, and creative tabs
- src/main/java/com/rave/projectbabylonweapons/item/
  - Weapon and tool item implementations by weapon family
- src/main/java/com/rave/projectbabylonweapons/network/
  - Packet registration and weapon-related networking
- src/main/java/com/rave/projectbabylonweapons/tooltip/
  - Shared weapon passive tooltip rendering and resolution

## Weapon passive system
- src/main/java/com/rave/projectbabylonweapons/passive/
  - Runtime weapon passive logic grouped by passive family
- src/main/java/com/rave/projectbabylonweapons/passive/*/*Passive.java
  - Owns effect logic, tooltip fallback visuals, and combat hooks
- src/main/java/com/rave/projectbabylonweapons/passive/*/*Balance.java
  - Owns built-in balance fallbacks and weapon-to-profile resolution
- src/main/java/com/rave/projectbabylonweapons/passive/data/
  - Datapack-driven passive patch infrastructure
  - WeaponPassiveIds defines passive ids used by runtime and data
  - WeaponPassivePatch defines generic passive patch entries for one item, many items, or an advanced item tag
  - WeaponPassivePatchManager loads datapack JSON from data/*/weapon_passive_patches/**/*.json

## Datapack passive patches
- Path: data/<namespace>/weapon_passive_patches/**/*.json
- Purpose:
  - Attach an existing Project Babylon passive to any concrete item id or item id list
  - Optionally attach a passive to an advanced item tag when ids are not practical
  - Override balance/profile values per passive without changing code
  - Override tooltip visual data for passive presentation without changing code
- Scope:
  - Runtime passive mechanics stay in code
  - Datapacks control target weapon selection, balance profile values, and optional tooltip visual data
- Patch shape:
  - exactly one selector:
    - weapon_id
    - weapon_ids
    - weapon_tag for advanced cases
  - passive_id
  - isual
    - display_name_key or display_name
    - rame_type
    - rame_texture
    - icon_texture
    - description_keys or description_lines
  - profile

## Archived code
- wiki/copy_del_file/
  - Stores deleted Java files that were removed during refactors