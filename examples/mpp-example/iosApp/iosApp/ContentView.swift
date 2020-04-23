//
//  ContentView.swift
//  iosApp
//
//  Created by PeterSamokhin on 22.04.2020.
//  Copyright © 2020 VKSDKKotlinExample. All rights reserved.
//

import SwiftUI

struct ContentView: View {
    @ObservedObject private var viewModel = ContentViewModel()
    
    var body: some View {
        Text(viewModel.message)
            .multilineTextAlignment(.center)
            .onAppear(perform: viewModel.onAttach)
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
