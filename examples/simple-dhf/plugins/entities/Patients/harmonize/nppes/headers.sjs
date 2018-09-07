/*
 * Create Headers Plugin
 *
 * @param id       - the identifier returned by the collector
 * @param content  - the output of your content plugin
 * @param options  - an object containing options. Options are sent from Java
 *
 * @return - an object of headers
 */
function createHeaders(id, content, options) {
  return {
    addressCityName: content["Provider Business Mailing Address City Name"],
    addressStateName: content["Provider Business Practice Location Address State Name"]
  };
}

module.exports = {
  createHeaders: createHeaders
};
