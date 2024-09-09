
    document.addEventListener('DOMContentLoaded', function() {
        const stateDropdown = document.getElementById('state');
        const cityDropdown = document.getElementById('city');

        // Free API endpoint for fetching states in India
        const statesApiEndpoint = 'https://countriesnow.space/api/v0.1/countries/states';
        const citiesApiEndpoint = 'https://countriesnow.space/api/v0.1/countries/state/cities';

        // Fetch states from the API
        fetch(statesApiEndpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                "country": "India"
            })
        })
        .then(response => response.json())
        .then(data => {
            const states = data.data.states;
            states.forEach(state => {
                const option = document.createElement('option');
                option.value = state.name;
                option.textContent = state.name;
                stateDropdown.appendChild(option);
            });
        })
        .catch(error => {
            console.error('Error fetching states:', error);
        });

        // Fetch cities based on the selected state
        stateDropdown.addEventListener('change', function() {
            const selectedState = stateDropdown.value;
            cityDropdown.innerHTML = '<option value="" disabled selected>Select your city</option>'; // Reset city dropdown

            fetch(citiesApiEndpoint, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    "country": "India",
                    "state": selectedState
                })
            })
            .then(response => response.json())
            .then(data => {
                const cities = data.data;
                cities.forEach(city => {
                    const option = document.createElement('option');
                    option.value = city;
                    option.textContent = city;
                    cityDropdown.appendChild(option);
                });
            })
            .catch(error => {
                console.error('Error fetching cities:', error);
            });
        });
    });
	
	
	
	

