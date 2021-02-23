import Foundation


@objc(PanoramaViewManager)
class PanoramaViewManager: RCTViewManager {
    
    override static func requiresMainQueueSetup() -> Bool {
        return true
    }
    
    
    // MARK: RN Methods
    
    override func view() -> UIView! {
        let res = PanoramaView()
        res.bridge = self.bridge
        return res
    }
}
