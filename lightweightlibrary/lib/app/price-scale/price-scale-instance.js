import FunctionManager from "../function-manager.js";
import PriceScaleCache from "./price-scale-cache.js";

export default class PriceScaleInstanceService {

    constructor(locator) {
        this.chart = locator.resolve("chart");
        this.priceScaleCache = locator.resolve(PriceScaleCache.name);
        this.functionManager = locator.resolve(FunctionManager.name);
    }

    register() {
        this._seriesInstanceMethods().forEach((method) => {
            this.functionManager.registerFunction(method.name, (input, resolve) => {
                const scale = this.priceScaleCache.get(input.params.caller)
                if (scale === undefined) {
                    this.functionManager.throwFatalError(new Error(`PriceScale with uuid:${input.caller} is not found`), input)
                } else {
                    method.invoke(scale,input.params,resolve)
                }
            });
        });

    }

    _seriesInstanceMethods() {
        return [
            new ApplyOptions(),
            new PriceScaleWidth(),
        ];
    }

    _priceLineMethods() {
        return [
            new PriceLineOptions(),
            new PriceLineApplyOptions()
        ];
    }

    _findSeries(input, callback) {
        let series = this.seriesCache.get(input.params.seriesId)
        if (series === undefined) {
            this.functionManager.throwFatalError(new Error(`${seriesName} with uuid:${input.uuid} is not found`), input)
        } else {
            callback(series)
        }
    }
}

/**
 * ==============================================================
 * Methods of series instance
 * ==============================================================
 */
class PriceScaleInstanceMethod {
    constructor(name, invoke) {
        this.name = name;
        this.invoke = invoke;
    }
}

class ApplyOptions extends PriceScaleInstanceMethod {
    constructor() {
        super("priceScaleApplyOptions", function(scale, params, resolve) {
            scale.applyOptions(params.options)
        });
    }
}


class PriceScaleWidth extends PriceScaleInstanceMethod {
    constructor() {
        super("priceScaleWidth", function(scale, params, resolve) {
            resolve(scale.width())
        });
    }
}
