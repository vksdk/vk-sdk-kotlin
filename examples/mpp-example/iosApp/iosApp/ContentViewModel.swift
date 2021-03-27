//
//  ContentViewModel.swift
//  iosApp
//
//  Created by PeterSamokhin on 22.04.2020.
//  Copyright © 2020 VKSDKKotlinExample. All rights reserved.
//

import Foundation
import SharedCode

class ContentViewModel: ObservableObject {
    private let presenter = SampleCommonPresenter()
    
    @Published var message = "Hello, VK SDK Kotlin!"
    
    /// After the view is ready, request and show some information
    func onAttach() {
        let pashka = presenter.getPashkaProfile()
        let pashkaFullName = "\(pashka?.firstName ?? "unknown") \(pashka?.lastName ?? "unknown")"

        self.message = "Pashka's full name:\n\(pashkaFullName)"
    }
}
