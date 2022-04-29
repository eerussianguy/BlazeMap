from mcresources import ResourceManager
from mcresources import utils


def generate(rm: ResourceManager):
    vanilla_woods = ('oak', 'acacia', 'dark_oak', 'birch', 'jungle', 'spruce', 'azalea', 'flowering_azalea')

    for wood in vanilla_woods:
        leaves(rm, '%s_leaves' % wood, '%s_fluff' % wood)

    pad = 0
    for flower in range(0, 1 + 1):
        for root in range(0, 2 + 1):
            rm.block_model('betterfoliage:lily_pad%d' % pad, parent='betterfoliage:block/lily_pad', textures={
                'flower': 'betterfoliage:block/lilypad_flower%d' % flower,
                'roots': 'betterfoliage:block/lilypad_roots%d' % root
            })
            pad += 1

    cactus_variants = [{'model': 'minecraft:block/cactus', 'weight': 3, 'y': i} for i in (0, 90, 180, 270)]
    cactus_variants.extend([{'model': 'betterfoliage:block/cactus1', 'weight': 2, 'y': i} for i in (0, 90, 180, 270)])
    cactus_variants.extend([{'model': 'betterfoliage:block/cactus2', 'weight': 4, 'y': i} for i in (0, 90, 180, 270)])
    cactus_variants.extend([{'model': 'betterfoliage:block/cactus3', 'y': i} for i in (0, 90, 180, 270)])
    cactus_variants.extend([{'model': 'betterfoliage:block/cactus4', 'y': i} for i in (0, 90, 180, 270)])
    cactus_variants.extend([{'model': 'betterfoliage:block/cactus5', 'y': i} for i in (0, 90, 180, 270)])

    rm.blockstate('minecraft:cactus', variants={"": cactus_variants}, use_default_model=False)

    rm.blockstate('minecraft:grass_block', variants={
        'snowy=false': {'model': 'betterfoliage:block/grass_block'},
        'snowy=true': {'model': 'betterfoliage:block/snowy_grass_block'}
    })

    rm.blockstate('minecraft:mycelium', variants={
        'snowy=false': {'model': 'betterfoliage:block/mycelium'},
        'snowy=true': {'model': 'betterfoliage:block/snowy_grass_block'}
    })

    rm.blockstate('minecraft:podzol', variants={
        'snowy=false': {'model': 'betterfoliage:block/podzol'},
        'snowy=true': {'model': 'betterfoliage:block/snowy_grass_block'}
    })

    direct_block_model(rm, 'betterfoliage:grass_block', {
        'loader': 'betterfoliage:grass',
        'dirt': 'minecraft:block/dirt',
        'top': 'minecraft:block/grass_block_top',
        'overlay': 'minecraft:block/grass_block_side_overlay',
        'tint': True,
        'grass': 'betterfoliage:block/better_grass'
    })

    direct_block_model(rm, 'betterfoliage:snowy_grass_block', {
        'loader': 'betterfoliage:grass',
        'dirt': 'minecraft:block/dirt',
        'top': 'minecraft:block/snow',
        'overlay': 'minecraft:block/grass_block_snow',
        'tint': False,
        'grass': 'betterfoliage:block/better_grass_snowed'
    })

    direct_block_model(rm, 'betterfoliage:mycelium', {
        'loader': 'betterfoliage:grass',
        'dirt': 'minecraft:block/dirt',
        'top': 'minecraft:block/mycelium_top',
        'overlay': 'minecraft:block/mycelium_side',
        'tint': False,
        'grass': 'betterfoliage:block/better_mycelium'
    })

    direct_block_model(rm, 'betterfoliage:podzol', {
        'loader': 'betterfoliage:grass',
        'dirt': 'minecraft:block/dirt',
        'top': 'minecraft:block/podzol_top',
        'overlay': 'minecraft:block/podzol_side',
        'tint': False
    })

    rm.block_model('better_grass', {'cross': 'betterfoliage:block/better_grass'}, parent='betterfoliage:block/tinted_cross_high')
    rm.block_model('better_mycelium', {'cross': 'betterfoliage:block/better_mycelium'}, parent='betterfoliage:block/cross_high')
    rm.block_model('better_grass_snowed', {'cross': 'betterfoliage:block/better_grass_snowed'}, parent='betterfoliage:block/cross_high')

    # enhanced farming
    for fruit in ('apple', 'avocado', 'banana', 'cherry', 'lemon', 'mango', 'olive', 'orange', 'pear'):
        base = 'oak' if fruit != 'banana' else 'jungle'
        leaves_model_only(rm, 'enhancedfarming:%s_leaves_fruity' % fruit, 'minecraft:block/%s_leaves' % base, 'betterfoliage:block/%s_fluff' % base, 'enhancedfarming:block/leaves/%s_leaves_fruity' % fruit)
        leaves_model_only(rm, 'enhancedfarming:%s_leaves_blooming' % fruit, 'minecraft:block/%s_leaves' % base, 'betterfoliage:block/%s_fluff' % base, 'enhancedfarming:block/leaves/%s_leaves_blooming' % fruit)


def leaves_model_only(rm: ResourceManager, model: str, block: str, fluff: str, overlay: str = None):
    direct_block_model(rm, model, utils.del_none({
        'loader': 'betterfoliage:leaves',
        'leaves': block,
        'fluff': fluff,
        'overlay': overlay
    }))


def leaves(rm: ResourceManager, name: str, fluff: str, overlay: str = None):
    rm.blockstate('minecraft:%s' % name, model='betterfoliage:block/%s' % name)
    direct_block_model(rm, 'betterfoliage:%s' % name, utils.del_none({
        'loader': 'betterfoliage:leaves',
        'leaves': 'minecraft:block/%s' % name,
        'fluff': 'betterfoliage:block/%s' % fluff,
        'overlay': overlay
    }))


def direct_block_model(rm: ResourceManager, location: utils.ResourceIdentifier, json: utils.Json):
    res = utils.resource_location(rm.domain, location)
    rm.write((*rm.resource_dir, 'assets', res.domain, 'models', 'block', res.path), json)
