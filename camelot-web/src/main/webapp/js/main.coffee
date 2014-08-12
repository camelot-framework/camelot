((window)->
  PluginsClientReciever = (basePath)->

    parseJson = (jsonOrJsons) ->
      jsonOrJsons = '[' + jsonOrJsons.replace(/}{/g, '},{') + ']'
      try
        res = JSON.parse(jsonOrJsons)
      catch e
        console.error('Failed to parse JSON: ' + e.message + '\n', jsonOrJsons)
      res

    sourcesMap = {}

    @unsubscribe = (path, topic)->
      topic = topic || ''
      url = basePath + '/plugins/' + path + '?topic=' + topic
      sourcesMap[url].close()
      delete sourcesMap[url]

    @subscribe = (path, topic, callback)->
      if typeof topic == 'function'
        callback = topic
        topic = ''

      url = basePath + '/plugins/' + path + '?topic=' + topic
      source = new EventSource(url);
      sourcesMap[url] = source

      source.onopen = ->
      source.onerror = ->

      source.onmessage = (response) ->
        $.each(parseJson(response.data), (idx, obj)->
          callback.call(this, obj)
        )

    this

  window.PluginsSystem = window.PluginsSystem || {}
  window.PluginsSystem.clientReciever = new PluginsClientReciever(window.PluginsSystem.contextPath)
)(window)
angular.module('camelotUtil', [])
  .constant('baseUrl', window.PluginsSystem.contextPath)
  .factory('subscribe', ['$window', '$rootScope', ($window, $rootScope) ->
    (pluginId, callback) ->
      $window.PluginsSystem.clientReciever.subscribe pluginId, (message) ->
        $rootScope.$apply ->
          callback message
  ])
