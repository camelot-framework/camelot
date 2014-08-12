camelotApp = angular.module("camelotDashboard", ["camelotUtil", "camelotDraggable", "localStorageModule"])
camelotApp.constant "baseUrl", window.PluginsSystem.contextPath
camelotApp.config ["$httpProvider", ($httpProvider) ->
  "use strict"
  $httpProvider.defaults.cache = true
]
camelotApp.controller "AppCtrl", ["$scope", "widgets", ($scope, widgets) ->
  "use strict"
  saveWidgets = () ->
    widgets.setActive $scope.widgets.map (col) ->
      col.map (w) -> w.name
  $scope.widgets = widgets.getActive()
  saveWidgets();
  $scope.$on "widgetRemove", (event, widgetName) ->
    $scope.widgets = $scope.widgets.map (col) ->
      col.filter (w) -> w.name != widgetName
    saveWidgets()
  $scope.$on "widgetAdd", (event, widgetName) ->
    $scope.widgets.reduce(
      (min, col) ->
        if min && min.length <= col.length
          min
        else
          col
    ).push widgets.getById(widgetName)
    saveWidgets()
  $scope.$on "widgetsReorder", (event, widgetList) ->
    widgets.setActive widgetList
]
camelotApp.factory "widgets", ["pluginsList", "$storage", (pluginsList, $storage) ->
  "use strict"
  class Widget
    constructor: (name, plugin) ->
      @hasWidget = !!plugin.context.widgetPath
      @hasDashboard = !!plugin.context.dashboardPath
      @module = plugin.context.pluginClass
      @name = name
  settingsWidget =
    name: 'settings',
    module: 'camelotSettingWidget'
  store = $storage "widgets"
  widgets = []
  angular.forEach pluginsList, (plugin, name) ->
    widgets.push new Widget name, plugin
  getById: (id) -> widgets.filter((w)-> w.name == id)[0]
  getAll: -> widgets
  getAllWidgets: ->
    @getAll().filter (w) -> w.hasWidget
  getActiveDefaults: ->
    @getAllWidgets()
      .map (w) -> w.name
      .concat 'settings'
      .reduce ((widgets, widget, i) ->
        widgets[(i+1) % 2].push(widget)
        widgets
      ), [[],[]]
  getActiveNames: ->
    `var widgets`
    widgets = store.getItem("list") or @getActiveDefaults()
    unless Array.isArray widgets[0]
      widgets = [widgets, []]
    widgets
  getActive: ->
    all = @getAllWidgets().concat settingsWidget
    @getActiveNames().map (list) ->
      list
        .map (name) ->
          all.filter( (w) -> w.name == name )[0]
        .filter (w) -> w
  setActive: (list) ->
    store.setItem "list", list
]
camelotApp.directive "widget", ["$compile", "$templateCache", 'baseUrl', ($compile, $templateCache, baseUrl) ->
    "use strict"
    replace: true
    template: '<div class="widget">
      <span dragger class="widget_move glyphicon glyphicon-move"></span>
      <span class="widget_close glyphicon glyphicon-remove-circle" ng-click="closeWidget()"></span>
      <a class="widget_open glyphicon glyphicon-share-alt" ng-show="widget.hasDashboard" ng-href="{{getDashboardUrl()}}"></a>
      <div class="panel panel-default">
        <div class="panel-body widget_body"></div>
      </div>
    </div>'
    scope:
      widget: "="
    link: (scope, elm) ->
      elm.addClass scope.widget.name
      elm = d3.select(elm[0])
      elm.data [scope.widget.name]
      body = elm.select(".widget_body").node()
      body.innerHTML = $templateCache.get(scope.widget.name)
      angular.element(body).data "$injector", null
      try
        pluginModule = scope.widget.name + "-plugin"
        angular.module(pluginModule, [scope.widget.module]).value "pluginId", scope.widget.name
        angular.bootstrap body, [pluginModule]
      catch e
        console.log "Plugin #{scope.widget.name} hasn't a module"
      scope.getDashboardUrl = ->
        "#{baseUrl}/plugin/#{scope.widget.name}"
      scope.closeWidget = ->
        scope.$emit "widgetRemove", scope.widget.name
]

settingsModule = angular.module 'camelotSettingWidget', ['camelotDashboard', 'ui.bootstrap']
settingsModule.controller 'WidgetCtrl', ['$scope', '$modal', ($scope, $modal)->
  $scope.openSettings = () ->
    $modal.open(
      templateUrl: 'modal.tpl',
      controller: 'ModalCtrl'
    );
]
settingsModule.controller 'ModalCtrl', ['$scope', '$rootElement', 'baseUrl', 'widgets', ($scope, $rootElement, baseUrl, widgets) ->
  parentAppScope = $rootElement.parent().injector().get('$rootScope')
  active = [].concat widgets.getActiveNames()...
  $scope.plugins = widgets.getAll().map (plugin) ->
    angular.extend {active: active.indexOf(plugin.name) != -1}, plugin
  $scope.getDashboardUrl = (plugin) ->
    "#{baseUrl}/plugin/#{plugin.name}"
  $scope.activate = (plugin) ->
    plugin.active = true
    parentAppScope.$broadcast('widgetAdd', plugin.name)
    parentAppScope.$apply();
  $scope.deactivate = (plugin) ->
    plugin.active = false
    parentAppScope.$broadcast('widgetRemove', plugin.name)
    parentAppScope.$apply();
]
