require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "PanoramaView"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.description  = <<-DESC
                  PanoramaView
                   DESC
  s.homepage     = "https://github.com/lightbasenl/react-native-panorama-view"
  s.license      = "MIT"
  s.license    = { :type => "MIT", :file => "LICENSE" }
  s.author       = { "author" => "rody@lightbase.nl" }
  s.platform     = :ios, "9.0"
  s.source       = { :git => "https://github.com/lightbase/react-native-panorama-view.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,m}"
  s.requires_arc = true
  s.swift_version = '5.2'

  s.dependency "React"

  
  s.subspec 'Core' do |ss|
    ss.dependency     'Lightbase-CTPanoramaView', "1.5"
  end
end

