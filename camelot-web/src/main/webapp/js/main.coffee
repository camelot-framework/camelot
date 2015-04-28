((window)->
  PluginsClientReciever = (basePath)->
    parseJson = (jsonOrJsons) ->
      jsonOrJsons = '[' + jsonOrJsons.replace(/}{/g, '},{') + ']'
      try
        res = JSON.parse(jsonOrJsons)
      catch e
        console.error('Failed to parse JSON: ' + e.message + '\n', jsonOrJsons)
      res

    getUrl = (pluginId, topic) ->
      "#{basePath}/websocket?pluginId=#{pluginId}&topic=#{topic}"

    sourcesMap = {}
    socket = $.atmosphere

    @unsubscribe = (pluginId, topic)->
      topic = topic || ''
      url = getUrl(pluginId, topic)
      socket.unsubscribe(sourcesMap[url]);
      delete sourcesMap[url]

    @subscribe = (pluginId, topic, callback)->
      if typeof topic == 'function'
        callback = topic
        topic = ''
      url = getUrl(pluginId, topic)
      sourcesMap[url] =
        url: url
        contentType: "application/json"
        logLevel: "debug"
        transport: "websocket"
        trackMessageLength: true
        fallbackTransport: "long-polling"
        onClose: ->
        onError: ->
        onMessage: (response) ->
          message = response.responseBody
          $.each(parseJson(message), (idx, obj)->
            callback.call(this, obj)
          )
      socket.subscribe(sourcesMap[url]);
    this

  window.PluginsSystem = window.PluginsSystem || {}
  window.PluginsSystem.clientReciever = new PluginsClientReciever(window.PluginsSystem.contextPath))(window)
angular.module('camelotUtil', [])
.constant('baseUrl', window.PluginsSystem.contextPath)
.factory('subscribe', ['$window', '$rootScope', ($window, $rootScope) ->
    (pluginId, callback) ->
      $window.PluginsSystem.clientReciever.subscribe pluginId, (message) ->
        $rootScope.$apply ->
          callback message
  ])
