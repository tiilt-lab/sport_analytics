//
//  AppDelegate.swift
//  HelloIndoor
//
//  Created by M Smith on 8/5/19.
//  Copyright © 2019 M Smith. All rights reserved.
//

import UIKit

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?


    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        // Override point for customization after application launch.
        
        let locationBuilder = EILLocationBuilder()
        // Step 1: add location name, like "tiilt_lab"
        locationBuilder.setLocationName("tiilt_lab")
        
        // Step 2: add beacon coordinates
        locationBuilder.setLocationBoundaryPoints([
            EILPoint(x: 0.00, y: 0.00),
            EILPoint(x: 1.00, y: 0.00),
            EILPoint(x: 1.00, y: 1.00),
            EILPoint(x: 0.00, y: 1.00)])
        
        // Step 3: add beacon ID's, and offsets from wall
        locationBuilder.addBeacon(withIdentifier: "7b6f239f7b2811c0e5f0e8094513f212",
                                  atBoundarySegmentIndex: 0, inDistance: 0.0, from: .leftSide)
        locationBuilder.addBeacon(withIdentifier: "6f324e5b8fdacee293241e78deeeb92d",
                                  atBoundarySegmentIndex: 1, inDistance: 0.0, from: .leftSide)
        locationBuilder.addBeacon(withIdentifier: "d1559116a15b2eaed2239837ce46f920",
                                  atBoundarySegmentIndex: 2, inDistance: 0.0, from: .leftSide)
        locationBuilder.addBeacon(withIdentifier: "ff6a43970191b949d315b17f83135d0f",
                                  atBoundarySegmentIndex: 3, inDistance: 0.0, from: .leftSide)
        
        // Step 4: input compass degrees of y axis
        locationBuilder.setLocationOrientation(280)
        
        let location = locationBuilder.build()!
        
        // Step OPTIONAL: change App_ID and App_Token
        ESTConfig.setupAppID("msmith-example--1-ne7", andAppToken: "b401ec550b49a18bd22622ee11bf0152")
        let addLocationRequest = EILRequestAddLocation(location: location)
        addLocationRequest.sendRequest { (location, error) in
            if error != nil {
                NSLog("Error when saving location: \(error)")
            } else {
                NSLog("Location saved successfully: \(location?.identifier)")
            }
        }
        
        return true
    }

    func applicationWillResignActive(_ application: UIApplication) {
        // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
        // Use this method to pause ongoing tasks, disable timers, and invalidate graphics rendering callbacks. Games should use this method to pause the game.
    }

    func applicationDidEnterBackground(_ application: UIApplication) {
        // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
        // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
    }

    func applicationWillEnterForeground(_ application: UIApplication) {
        // Called as part of the transition from the background to the active state; here you can undo many of the changes made on entering the background.
    }

    func applicationDidBecomeActive(_ application: UIApplication) {
        // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
    }

    func applicationWillTerminate(_ application: UIApplication) {
        // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
    }


}

