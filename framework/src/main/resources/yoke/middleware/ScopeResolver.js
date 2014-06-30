/**
 * This is a very simple middleware. We just need a way for JSYokeRequest and
 * JSYokeResponse to know its executing scope.
 */
module.exports = function(request, next) {
    request.resolveScope();
    next(null);
};
