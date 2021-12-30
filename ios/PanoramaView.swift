import Foundation


@objc public class PanoramaView: UIView {


    // MARK: Public properties

    @objc public var bridge : RCTBridge? = nil
    @objc public var onImageLoadingFailed: RCTDirectEventBlock? = nil
    @objc public var onImageDownloaded: RCTDirectEventBlock? = nil
    @objc public var onImageLoaded: RCTDirectEventBlock? = nil

    @objc public var enableTouchTracking: Bool = true {
        didSet {
            if(enableTouchTracking){
                self.panoramaView?.controlMethod = .both;
            }
            else{
                self.panoramaView?.controlMethod = .motion;
            }
        }
    }
    
    private static var metalSize: Int = {
        var maxTexSize = 8192
        if let device = MTLCreateSystemDefaultDevice() {
            if #available(iOS 11.0, *) {
                if device.supportsFeatureSet(.iOS_GPUFamily4_v1) || device.supportsFeatureSet(.iOS_GPUFamily3_v1) {
                    maxTexSize = 16384
                } else if device.supportsFeatureSet(.iOS_GPUFamily2_v2) || device.supportsFeatureSet(.iOS_GPUFamily1_v2) {
                    maxTexSize = 8192
                } else {
                    maxTexSize = 4096
                }
            } else {
                if device.supportsFeatureSet(.iOS_GPUFamily2_v2) || device.supportsFeatureSet(.iOS_GPUFamily1_v2) {
                    maxTexSize = 8192
                } else {
                    maxTexSize = 4096
                }
            }
        }
        return maxTexSize
    }()

    @objc public var imageUrl: String? = nil {
        didSet {
            if(!(imageUrl?.isEmpty ?? true)){

                // this runs in the UI thread so it is fine to call it here
                if(self.bridge == nil){
                    self.onImageLoadingFailed?(["error": "Bridge is not ready or not set."])
                    return
                }

                // ugly part of using RN's objective C code in Swift
                let loader = self.bridge?.module(forName: "ImageLoader", lazilyLoadIfNecessary: true) as! RCTImageLoader

                self.cancel?()
                self.cancel = nil

                guard let request = RCTConvert.nsurlRequest(imageUrl) else {
                    return
                }

                self.cancel = loader.loadImage(with: request, callback: { (error, image) in

                    DispatchQueue.main.async {

                        // do nothing if we have cancelled the request
                        if(self.cancel == nil){
                            return
                        }

                        if (error != nil) {
                            self.onImageLoadingFailed?(["error": error!.localizedDescription])
                        }
                        else{
                            self.onImageDownloaded?(nil)

                            if let _image = image {
                                var resized = _image
                                let size = _image.size
                                
                                // Resize image if too big or the viewer will choke
                                // Get max metal supported size to prevent
                                // 'Texture Descriptor Validation MTLTextureDescriptor has width (XXXX) greater than the maximum allowed size of 16384.' errors
                                let targetSize = CGSize(width: PanoramaView.metalSize, height: PanoramaView.metalSize)
                                
                                if size.width > targetSize.width || size.height > targetSize.height {
                                    
                                    let widthRatio  = targetSize.width / size.width
                                    let heightRatio = targetSize.height / size.height

                                    // Figure out what our orientation is, and use that to form the rectangle
                                    var newSize: CGSize
                                    if(widthRatio > heightRatio) {
                                       newSize = CGSize(width: size.width * heightRatio, height: size.height * heightRatio)
                                    } else {
                                       newSize = CGSize(width: size.width * widthRatio, height: size.height * widthRatio)
                                    }
                                    
                                    
                                    // This is the rect that we've calculated out and this is what is actually used below
                                    let rect = CGRect(origin: .zero, size: newSize)

                                    // Actually do the resizing to the rect using the ImageContext stuff
                                    UIGraphicsBeginImageContextWithOptions(newSize, false, 1.0)
                                    
                                    _image.draw(in: rect)
                                    
                                    resized = UIGraphicsGetImageFromCurrentImageContext() ?? resized
                                    
                                    UIGraphicsEndImageContext()
                                }
                                
                                self.panoramaView?.image = resized
                                self.onImageLoaded?(nil)
                            }
                            else{
                                self.onImageLoadingFailed?(["error": "Image was empty or failed to load."])
                            }
                        }

                        self.cancel = nil
                    }
                })
            }
            else{
                self.cancel?()
                self.cancel = nil
            }
        }
    }



    // MARK: Private properties

    private var panoramaView : CTPanoramaView? = nil
    private var cancel : RCTImageLoaderCancellationBlock? = nil


    // MARK: Class lifecycle methods

    public override init(frame: CGRect) {
        super.init(frame: frame)
        let view = CTPanoramaView(frame: frame)
        view.startAngle = 3.45
        view.controlMethod = .both;

        self.panoramaView = view
        self.addSubview(view)
    }


    public required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    deinit {
        self.cancel?()
        self.cancel = nil
    }

    public override func layoutSubviews() {
        super.layoutSubviews()
        self.panoramaView?.frame = CGRect(x: 0, y: 0, width: frame.size.width, height: frame.size.height)
        self.panoramaView?.setNeedsDisplay()
    }

    public override func willMove(toSuperview newSuperview: UIView?){
        super.willMove(toSuperview: newSuperview)

        // cleanup/cancel downloads on view removal
        if(newSuperview == nil){
            self.cancel?()
            self.cancel = nil
        }
    }
}
