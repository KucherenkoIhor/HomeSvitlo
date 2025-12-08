import Foundation

struct SolaxApiResponse: Codable {
    let success: Bool
    let exception: String?
    let result: SolaxResult?
    let code: Int
}

struct SolaxResult: Codable {
    let inverterStatus: String?
    let soc: Double?
}

class SolaxApiClient {
    static let shared = SolaxApiClient()
    
    // TODO: Move to secure config
    private let wifiSn = "SN6MBN9GUZ"
    private let tokenId = "20251208145659068702972"
    private let baseUrl = "https://global.solaxcloud.com/api/v2/dataAccess/realtimeInfo/get"
    
    private init() {}
    
    func fetchInverterStatus(completion: @escaping (Result<(statusCode: String, batteryCharge: Double), Error>) -> Void) {
        guard let url = URL(string: baseUrl) else {
            completion(.failure(NSError(domain: "SolaxApi", code: -1, userInfo: [NSLocalizedDescriptionKey: "Invalid URL"])))
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue(tokenId, forHTTPHeaderField: "tokenId")
        
        let body = ["wifiSn": wifiSn]
        request.httpBody = try? JSONEncoder().encode(body)
        
        URLSession.shared.dataTask(with: request) { data, response, error in
            if let error = error {
                DispatchQueue.main.async {
                    completion(.failure(error))
                }
                return
            }
            
            guard let data = data else {
                DispatchQueue.main.async {
                    completion(.failure(NSError(domain: "SolaxApi", code: -2, userInfo: [NSLocalizedDescriptionKey: "No data"])))
                }
                return
            }
            
            do {
                let response = try JSONDecoder().decode(SolaxApiResponse.self, from: data)
                
                if response.success, let result = response.result {
                    let statusCode = result.inverterStatus ?? ""
                    let batteryCharge = result.soc ?? 0.0
                    DispatchQueue.main.async {
                        completion(.success((statusCode: statusCode, batteryCharge: batteryCharge)))
                    }
                } else {
                    DispatchQueue.main.async {
                        completion(.failure(NSError(domain: "SolaxApi", code: response.code, userInfo: [NSLocalizedDescriptionKey: response.exception ?? "Unknown error"])))
                    }
                }
            } catch {
                DispatchQueue.main.async {
                    completion(.failure(error))
                }
            }
        }.resume()
    }
}

