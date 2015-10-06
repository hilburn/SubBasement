Sub-Basement
=========
A requested mod to prevent block placement by dimension and y-level.

Syntax (config/SubBasement.json):

    [
        {
            "dim": NUMBER,
            "minDim": NUMBER,
            "maxDim": NUMBER,
            "y": NUMBER,
            "minY": NUMBER,
            "maxY": NUMBER
        },
        .... etc
    ]

All of these vars are optional, min and max values are defaulted to +/- infinity so if you just set a max it will capture everything below, if you just set a min, everything above, if you set the "dim", or "y" values it's just a shorthand for setting the min and max values to the same number.

## Contributors
hilburn

## License
Licensed under the [DBaJ (Don't Be a Jerk) non-commercial care-free license](https://github.com/hilburn/NotEnoughResources/blob/master/LICENSE.md).
