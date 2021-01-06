require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "PanoramaView"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.description  = <<-DESC
                  PanoramaView
                   DESC
  s.homepage     = "https://github.com/author/PanoramaView"
  s.license      = "MIT"
  # s.license    = { :type => "MIT", :file => "FILE_LICENSE" }
  s.author       = { "author" => "author@domain.cn" }
  s.platform     = :ios, "10.0"
  s.source       = { :git => "https://github.com/lightbase/react-native-panorama-view.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,m,swift}"
  s.requires_arc = true
  s.swift_version = '5.3'
  s.frameworks = 'UIKit'
  s.dependency "React"
end

