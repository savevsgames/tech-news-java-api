document.getElementById('editPostForm').addEventListener('submit', function(event) {
    event.preventDefault();  // Prevent the default form submission
    const url = window.location.href;
    const postId = url.split("/").pop();

    const title = document.querySelector('input[name="title"]').value;

    fetch(`/api/posts/${postId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ title: title })
    })
        .then(response => response.json())
        .then(data => {
            console.log('Post updated:', data);
            // Optionally, redirect or update the UI
        })
        .catch(error => console.error('Error updating post:', error));
});