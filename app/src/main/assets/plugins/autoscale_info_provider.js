window.autoscaleInfoProvider = (pluginParams) => {
    return (original) => {
        var res = original();
        if (res.priceRange !== null) {
            res.priceRange.minValue -= 0;
            res.priceRange.maxValue -= res.priceRange.maxValue*0.01;
        }
        return res;
    }
}