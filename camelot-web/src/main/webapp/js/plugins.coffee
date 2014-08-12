$ ->
  toggleLoader = ->
    $('.reload-plugins-loader, button.reload-plugins').toggle();

  $("button.reload-plugins").click ->
    toggleLoader()
    $.ajax(
      method: "post"
      url: window.PluginsSystem.contextPath + '/plugins/reload'
    ).success(->
      console.log('Plugins classes reloaded successfully!')
      toggleLoader()
    )