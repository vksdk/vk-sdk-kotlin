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
        presenter.getPashkaProfileAsync { pashka in
            // Use the data class
            let pashkaFullName = "\(pashka.firstName) \(pashka.lastName)"

            self.message = "VK and Telegram CEO is:\n\(pashkaFullName)"
        }
    }
}
