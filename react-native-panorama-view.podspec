require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = package["name"]
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = package["homepage"]
  s.license      = package["license"]
  s.authors      = package["author"]
  s.platform     = :ios, "10.0"

  s.source       = { :git => "https://github.com/lightbase/react-native-panorama-view.git", :tag => "#{s.version}" }
  s.source_files = "ios/**/*.{h,m,swift}"
  s.public_header_files = 'ios/**/*.h'

  s.swift_version = '5.3'

  s.frameworks = 'UIKit'
  s.dependency "React"
end

