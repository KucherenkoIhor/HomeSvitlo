import SwiftUI
import ComposeApp

struct ContentView: View {
    @State private var showDebug = false
    
    var body: some View {
        ZStack(alignment: .topTrailing) {
            ComposeView()
                .ignoresSafeArea(.all)
            
            // Debug button (small, in corner)
            Button(action: { showDebug = true }) {
                Image(systemName: "ladybug")
                    .font(.system(size: 16))
                    .foregroundColor(.gray)
                    .padding(8)
                    .background(Color.white.opacity(0.8))
                    .clipShape(Circle())
            }
            .padding(.top, 50)
            .padding(.trailing, 16)
        }
        .sheet(isPresented: $showDebug) {
            DebugView()
        }
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
